package com.nexus.staffing.service.impl;

import com.nexus.employee.model.Absense;
import com.nexus.employee.model.Employee;
import com.nexus.employee.model.EmployeeResponsibility;
import com.nexus.employee.repository.AbsenseRepository;
import com.nexus.employee.repository.EmployeeRepository;
import com.nexus.employee.repository.EmployeeResponsibilityRepository;
import com.nexus.organization.repository.CampusRepository;
import com.nexus.project.model.Project;
import com.nexus.project.repository.ProjectRepository;
import com.nexus.shared.security.context.CurrentUserContext;
import com.nexus.shared.security.provider.CurrentUserProvider;
import com.nexus.staffing.allocationrequest.AllocationRequestMetadataParser;
import com.nexus.staffing.allocationrequest.AllocationRequestParsedMetadata;
import com.nexus.staffing.dto.request.resourcemanager.ReviewAllocationRequestDecision;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerAllocationRequestDetailsResponse;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerAllocationRequestListItemResponse;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerEmployeeAbsenceResponse;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerEmployeeEngagementResponse;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerRequestConflictResponse;
import com.nexus.staffing.mapper.ResourceManagerStaffingMapper;
import com.nexus.staffing.model.AllocationRequest;
import com.nexus.staffing.model.Engagement;
import com.nexus.staffing.model.EngagementDecision;
import com.nexus.staffing.model.enums.EngagementDecisionType;
import com.nexus.staffing.model.enums.EngagementStatus;
import com.nexus.staffing.repository.AllocationRequestRepository;
import com.nexus.staffing.repository.EngagementDecisionRepository;
import com.nexus.staffing.repository.EngagementRepository;
import com.nexus.staffing.resourcemanager.ResourceManagerAllocationRequestContext;
import com.nexus.staffing.service.ResourceManagerRequestService;
import com.nexus.staffing.util.ResourceManagerAvailabilityEvaluator;
import com.nexus.staffing.util.ResourceManagerAvailabilityEvaluationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResourceManagerRequestServiceImpl implements ResourceManagerRequestService {

    private static final String ROLE_RESOURCE_MANAGER = "RESOURCE_MANAGER";
    private static final String RESPONSIBILITY_TYPE_RESOURCE_MANAGER = "RESOURCE_MANAGER";

    private final CurrentUserProvider currentUserProvider;
    private final EmployeeRepository employeeRepository;
    private final EmployeeResponsibilityRepository responsibilityRepository;
    private final ProjectRepository projectRepository;
    private final AllocationRequestRepository allocationRequestRepository;
    private final EngagementRepository engagementRepository;
    private final EngagementDecisionRepository engagementDecisionRepository;
    private final AbsenseRepository absenseRepository;
    private final ResourceManagerStaffingMapper mapper;
    private final ResourceManagerAvailabilityEvaluator availabilityEvaluator;
    private final CampusRepository campusRepository;

    @Override
    public List<ResourceManagerAllocationRequestListItemResponse> listPendingRequests() {
        Employee resourceManager = requireCurrentResourceManagerEmployee();
        Set<Long> managedEmployeeIds = managedEmployeeIds(resourceManager.getId());
        if (managedEmployeeIds.isEmpty()) {
            return List.of();
        }

        List<Engagement> managedEngagements = engagementRepository
                .findByEmployeeIdInAndStatusIn(List.copyOf(managedEmployeeIds), List.of(EngagementStatus.PLANNED));
        if (managedEngagements.isEmpty()) {
            return List.of();
        }

        Map<Long, List<Engagement>> engagementsByRequestId = managedEngagements.stream()
                .collect(Collectors.groupingBy(Engagement::getAllocationRequestId));

        List<Long> requestIds = managedEngagements.stream()
                .map(Engagement::getAllocationRequestId)
                .distinct()
                .toList();

        List<AllocationRequest> requests = allocationRequestRepository.findByIdInOrderByCreatedAtDesc(requestIds);
        Map<Long, Employee> employeesById = loadEmployeesById(managedEngagements, requests);
        Map<Long, Project> projectsById = loadProjectsById(requests);

        List<Long> engagementIds = managedEngagements.stream().map(Engagement::getId).toList();
        Map<Long, List<EngagementDecision>> decisionsByEngagementId = engagementDecisionRepository
                .findByEngagementIdIn(engagementIds)
                .stream()
                .collect(Collectors.groupingBy(EngagementDecision::getEngagementId));

        return requests.stream()
                .map(request -> toPendingListItemOrNull(
                        request,
                        engagementsByRequestId.getOrDefault(request.getId(), List.of()),
                        decisionsByEngagementId,
                        projectsById,
                        employeesById))
                .filter(item -> item != null)
                .toList();
    }

    @Override
    public ResourceManagerAllocationRequestDetailsResponse getRequestDetails(Long requestId) {
        ResourceManagerAllocationRequestContext context = buildRequestContext(requestId, false);
        return toDetailsResponse(context);
    }

    @Override
    @Transactional
    public ResourceManagerAllocationRequestDetailsResponse approveRequest(
            Long requestId,
            ReviewAllocationRequestDecision decision) {
        ResourceManagerAllocationRequestContext context = buildRequestContext(requestId, true);

        if (context.primaryEngagement().getStatus() != EngagementStatus.PLANNED
                && context.primaryEngagement().getStatus() != EngagementStatus.ACTIVE) {
            throw badRequest("Only planned or active engagements can be approved");
        }

        EngagementDecision decisionEntity = mapper.toEngagementDecision(
                context.primaryEngagement(),
                context.resourceManager().getId(),
                EngagementDecisionType.APPROVED,
                normalizedComment(decision));
        engagementDecisionRepository.save(decisionEntity);

        context.primaryEngagement().setStatus(EngagementStatus.ACTIVE);
        engagementRepository.save(context.primaryEngagement());

        return toDetailsResponse(buildRequestContext(requestId, false));
    }

    @Override
    @Transactional
    public ResourceManagerAllocationRequestDetailsResponse rejectRequest(
            Long requestId,
            ReviewAllocationRequestDecision decision) {
        ResourceManagerAllocationRequestContext context = buildRequestContext(requestId, true);

        if (context.primaryEngagement().getStatus() != EngagementStatus.PLANNED
                && context.primaryEngagement().getStatus() != EngagementStatus.ACTIVE) {
            throw badRequest("Only planned or active engagements can be rejected");
        }

        EngagementDecision decisionEntity = mapper.toEngagementDecision(
                context.primaryEngagement(),
                context.resourceManager().getId(),
                EngagementDecisionType.REJECTED,
                normalizedComment(decision));
        engagementDecisionRepository.save(decisionEntity);

        context.primaryEngagement().setStatus(EngagementStatus.CANCELLED);
        engagementRepository.save(context.primaryEngagement());

        return toDetailsResponse(buildRequestContext(requestId, false));
    }

    private ResourceManagerAllocationRequestListItemResponse toPendingListItemOrNull(
            AllocationRequest request,
            List<Engagement> managedRequestEngagements,
            Map<Long, List<EngagementDecision>> decisionsByEngagementId,
            Map<Long, Project> projectsById,
            Map<Long, Employee> employeesById) {

        if (managedRequestEngagements.isEmpty()) {
            return null;
        }

        boolean isPendingForManagedEmployees = managedRequestEngagements.stream()
                .noneMatch(engagement -> !decisionsByEngagementId
                        .getOrDefault(engagement.getId(), List.of())
                        .isEmpty());

        if (!isPendingForManagedEmployees) {
            return null;
        }

        Engagement primaryEngagement = managedRequestEngagements.stream()
                .sorted(Comparator.comparing(Engagement::getId))
                .findFirst()
                .orElse(null);

        if (primaryEngagement == null) {
            return null;
        }

        AllocationRequestParsedMetadata metadata = AllocationRequestMetadataParser.parse(request.getComment());
        String requiredRole = metadata.requiredRole() != null ? metadata.requiredRole() : primaryEngagement.getRoleOnProject();
        String engagementLevel = metadata.engagementLevel() != null
                ? metadata.engagementLevel()
                : primaryEngagement.getEngagementLevel();
        LocalDate requestedStart = metadata.startDate() != null ? metadata.startDate() : primaryEngagement.getStartDate();
        LocalDate requestedEnd = metadata.endDate() != null ? metadata.endDate() : primaryEngagement.getEndDate();

        Project project = projectsById.get(request.getProjectId());
        Employee pm = employeesById.get(request.getCreatedByEmployeeId());
        Employee employee = employeesById.get(primaryEngagement.getEmployeeId());

        if (project == null || pm == null || employee == null) {
            return null;
        }

        return mapper.toAllocationRequestListItemResponse(
                request,
                project,
                pm,
                employee,
                requiredRole,
                engagementLevel,
                requestedStart,
                requestedEnd,
                "PENDING");
    }

    private ResourceManagerAllocationRequestDetailsResponse toDetailsResponse(ResourceManagerAllocationRequestContext context) {
        List<Engagement> overlappingEngagements = engagementRepository.findOverlappingEngagements(
                        context.employee().getId(),
                        context.requestedStartDate(),
                        context.requestedEndDate(),
                        List.of(EngagementStatus.PLANNED, EngagementStatus.ACTIVE))
                .stream()
                .filter(engagement -> !engagement.getId().equals(context.primaryEngagement().getId()))
                .toList();

        List<Absense> overlappingAbsences = absenseRepository.findOverlappingAbsences(
                context.employee().getId(),
                context.requestedStartDate(),
                context.requestedEndDate());

        List<ResourceManagerEmployeeEngagementResponse> overlappingEngagementResponses =
                mapEngagementResponses(overlappingEngagements);

        List<ResourceManagerEmployeeAbsenceResponse> overlappingAbsenceResponses = overlappingAbsences.stream()
                .sorted(Comparator.comparing(Absense::getStart))
                .map(mapper::toEmployeeAbsenceResponse)
                .toList();

        ResourceManagerAvailabilityEvaluationResult evaluation = availabilityEvaluator.evaluate(
                overlappingAbsences,
                overlappingEngagements,
                context.engagementLevel(),
                context.requestedStartDate(),
                context.requestedEndDate());

        ResourceManagerRequestConflictResponse conflictResponse = new ResourceManagerRequestConflictResponse(
                evaluation.hasAbsenceConflict(),
                evaluation.overlappingEngagementCount(),
                evaluation.hasEngagementLevelConflict(),
                evaluation.overlappingEngagementLoad(),
                evaluation.requestedEngagementLoad(),
                evaluation.availableForRequestedPeriod(),
                evaluation.explanations());

        String projectCampusName = campusRepository
                .findById(context.project().getOwnerCampusId())
                .map(campus -> campus.getName())
                .orElse(null);

        return mapper.toAllocationRequestDetailsResponse(
                context.request(),
                context.project(),
                context.projectManager(),
                context.employee(),
                context.requiredRole(),
                context.requiredSkill(),
                context.engagementLevel(),
                context.requestedStartDate(),
                context.requestedEndDate(),
                context.comment(),
                context.status(),
                overlappingEngagementResponses,
                overlappingAbsenceResponses,
                conflictResponse,
                projectCampusName);
    }

    private ResourceManagerAllocationRequestContext buildRequestContext(Long requestId, boolean requirePending) {
        Employee resourceManager = requireCurrentResourceManagerEmployee();

        AllocationRequest request = allocationRequestRepository.findById(requestId)
                .orElseThrow(() -> notFound("Allocation request not found with id " + requestId));

        Set<Long> managedEmployeeIds = managedEmployeeIds(resourceManager.getId());
        if (managedEmployeeIds.isEmpty()) {
            throw forbidden("You are not allowed to review this request");
        }

        List<Engagement> requestEngagements = engagementRepository.findByAllocationRequestId(requestId);
        List<Engagement> managedRequestEngagements = requestEngagements.stream()
                .filter(engagement -> managedEmployeeIds.contains(engagement.getEmployeeId()))
                .sorted(Comparator.comparing(Engagement::getId))
                .toList();

        if (managedRequestEngagements.isEmpty()) {
            throw forbidden("You are not allowed to review this request");
        }

        Engagement primaryEngagement = managedRequestEngagements.getFirst();

        List<Long> managedEngagementIds = managedRequestEngagements.stream().map(Engagement::getId).toList();
        Map<Long, List<EngagementDecision>> decisionsByEngagementId = engagementDecisionRepository
                .findByEngagementIdIn(managedEngagementIds)
                .stream()
                .collect(Collectors.groupingBy(EngagementDecision::getEngagementId));

        String status = resolveStatus(managedRequestEngagements, decisionsByEngagementId);
        if (requirePending && !"PENDING".equals(status)) {
            throw badRequest("Allocation request is already decided for managed employee(s)");
        }

        AllocationRequestParsedMetadata metadata = AllocationRequestMetadataParser.parse(request.getComment());
        String requiredRole = metadata.requiredRole() != null ? metadata.requiredRole() : primaryEngagement.getRoleOnProject();
        String requiredSkill = metadata.requiredSkill();
        String engagementLevel = metadata.engagementLevel() != null
                ? metadata.engagementLevel()
                : primaryEngagement.getEngagementLevel();
        LocalDate requestedStart = metadata.startDate() != null ? metadata.startDate() : primaryEngagement.getStartDate();
        LocalDate requestedEnd = metadata.endDate() != null ? metadata.endDate() : primaryEngagement.getEndDate();

        if (requestedStart == null) {
            throw badRequest("Requested start date is missing in allocation request metadata");
        }

        Employee employee = employeeRepository.findById(primaryEngagement.getEmployeeId())
                .orElseThrow(() -> notFound("Employee not found with id " + primaryEngagement.getEmployeeId()));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> notFound("Project not found with id " + request.getProjectId()));

        Employee projectManager = employeeRepository.findById(request.getCreatedByEmployeeId())
                .orElseThrow(() -> notFound("Project manager employee not found with id " + request.getCreatedByEmployeeId()));

        return new ResourceManagerAllocationRequestContext(
                resourceManager,
                request,
                project,
                projectManager,
                employee,
                primaryEngagement,
                requiredRole,
                requiredSkill,
                engagementLevel,
                requestedStart,
                requestedEnd,
                metadata.userComment(),
                status);
    }

    private String resolveStatus(
            List<Engagement> managedRequestEngagements,
            Map<Long, List<EngagementDecision>> decisionsByEngagementId) {

        boolean hasApproved = managedRequestEngagements.stream()
                .flatMap(engagement -> decisionsByEngagementId.getOrDefault(engagement.getId(), List.of()).stream())
                .anyMatch(decision -> decision.getDecision() == EngagementDecisionType.APPROVED);
        if (hasApproved) {
            return "APPROVED";
        }

        boolean hasRejected = managedRequestEngagements.stream()
                .flatMap(engagement -> decisionsByEngagementId.getOrDefault(engagement.getId(), List.of()).stream())
                .anyMatch(decision -> decision.getDecision() == EngagementDecisionType.REJECTED);
        if (hasRejected) {
            return "REJECTED";
        }

        return "PENDING";
    }

    private List<ResourceManagerEmployeeEngagementResponse> mapEngagementResponses(List<Engagement> engagements) {
        if (engagements.isEmpty()) {
            return List.of();
        }

        List<Long> requestIds = engagements.stream()
                .map(Engagement::getAllocationRequestId)
                .distinct()
                .toList();

        Map<Long, AllocationRequest> requestById = allocationRequestRepository.findByIdInOrderByCreatedAtDesc(requestIds)
                .stream()
                .collect(Collectors.toMap(AllocationRequest::getId, request -> request));

        List<Long> projectIds = requestById.values().stream()
                .map(AllocationRequest::getProjectId)
                .distinct()
                .toList();

        Map<Long, Project> projectById = projectRepository.findAllById(projectIds)
                .stream()
                .collect(Collectors.toMap(Project::getId, project -> project));

        return engagements.stream()
                .sorted(Comparator.comparing(Engagement::getStartDate))
                .map(engagement -> {
                    AllocationRequest request = requestById.get(engagement.getAllocationRequestId());
                    Project project = request == null ? null : projectById.get(request.getProjectId());
                    Long projectId = project == null ? null : project.getId();
                    String projectName = project == null ? null : project.getName();
                    return mapper.toEmployeeEngagementResponse(engagement, projectId, projectName);
                })
                .toList();
    }

    private String normalizedComment(ReviewAllocationRequestDecision decision) {
        if (decision == null || decision.comment() == null || decision.comment().isBlank()) {
            return null;
        }
        return decision.comment().trim();
    }

    private Set<Long> managedEmployeeIds(Long resourceManagerEmployeeId) {
        return responsibilityRepository
                .findAllByResponsibleIdAndTypeAndActiveTrueOrderByStartDateDesc(
                        resourceManagerEmployeeId,
                        RESPONSIBILITY_TYPE_RESOURCE_MANAGER)
                .stream()
                .map(EmployeeResponsibility::getEmployee)
                .map(Employee::getId)
                .collect(Collectors.toSet());
    }

    private Map<Long, Employee> loadEmployeesById(List<Engagement> engagements, List<AllocationRequest> requests) {
        Set<Long> ids = engagements.stream().map(Engagement::getEmployeeId).collect(Collectors.toSet());
        ids.addAll(requests.stream().map(AllocationRequest::getCreatedByEmployeeId).collect(Collectors.toSet()));

        Map<Long, Employee> employeesById = new HashMap<>();
        employeeRepository.findAllById(ids).forEach(employee -> employeesById.put(employee.getId(), employee));
        return employeesById;
    }

    private Map<Long, Project> loadProjectsById(List<AllocationRequest> requests) {
        List<Long> projectIds = requests.stream().map(AllocationRequest::getProjectId).distinct().toList();
        Map<Long, Project> projectsById = new HashMap<>();
        projectRepository.findAllById(projectIds).forEach(project -> projectsById.put(project.getId(), project));
        return projectsById;
    }

    private Employee requireCurrentResourceManagerEmployee() {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        if (!caller.hasRole(ROLE_RESOURCE_MANAGER)) {
            throw forbidden("Only resource managers can access this endpoint");
        }

        return employeeRepository.findByAuthUser(caller.userId())
                .orElseThrow(() -> notFound("Employee profile not found for authenticated resource manager"));
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}

package com.nexus.staffing.service.impl;

import com.nexus.employee.model.Employee;
import com.nexus.employee.repository.EmployeeRepository;
import com.nexus.employee.service.EmployeeService;
import com.nexus.project.model.Project;
import com.nexus.project.repository.ProjectRepository;
import com.nexus.shared.security.context.CurrentUserContext;
import com.nexus.shared.security.provider.CurrentUserProvider;
import com.nexus.staffing.allocationrequest.AllocationRequestMetadataParser;
import com.nexus.staffing.allocationrequest.AllocationRequestParsedMetadata;
import com.nexus.staffing.dto.request.allocationrequest.CreateAllocationRequest;
import com.nexus.staffing.dto.response.allocationrequest.AllocationRequestResponse;
import com.nexus.staffing.dto.response.allocationrequest.AllocationRequestTrackingStatus;
import com.nexus.staffing.dto.response.engagement.ProjectTeamMemberResponse;
import com.nexus.staffing.mapper.ProjectManagerStaffingMapper;
import com.nexus.staffing.model.AllocationRequest;
import com.nexus.staffing.model.Engagement;
import com.nexus.staffing.model.EngagementDecision;
import com.nexus.staffing.model.enums.EngagementDecisionType;
import com.nexus.staffing.model.enums.EngagementStatus;
import com.nexus.staffing.repository.AllocationRequestRepository;
import com.nexus.staffing.repository.EngagementDecisionRepository;
import com.nexus.staffing.repository.EngagementRepository;
import com.nexus.staffing.service.ProjectManagerStaffingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectManagerStaffingServiceImpl implements ProjectManagerStaffingService {

    private static final String ROLE_PROJECT_MANAGER = "PROJECT_MANAGER";
    private static final String METADATA_MARKER = "[[PM_REQUEST_METADATA]]";

    private final AllocationRequestRepository allocationRequestRepository;
    private final EngagementRepository engagementRepository;
    private final EngagementDecisionRepository engagementDecisionRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;
    private final CurrentUserProvider currentUserProvider;
    private final ProjectManagerStaffingMapper staffingMapper;

    @Override
    public List<ProjectTeamMemberResponse> listProjectTeam(Long projectId) {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        requireOwnedProject(caller, projectId);

        List<AllocationRequest> requests = allocationRequestRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
        if (requests.isEmpty()) {
            return List.of();
        }

        List<Long> requestIds = requests.stream().map(AllocationRequest::getId).toList();
        List<Engagement> engagements = engagementRepository.findByAllocationRequestIdIn(requestIds);
        if (engagements.isEmpty()) {
            return List.of();
        }

        Map<Long, Employee> employeesById = loadEmployeesById(engagements.stream()
                .map(Engagement::getEmployeeId)
                .collect(Collectors.toSet()));

        return engagements.stream()
            .map(engagement -> staffingMapper.toProjectTeamMemberResponse(
                engagement,
                employeesById.get(engagement.getEmployeeId())))
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing(ProjectTeamMemberResponse::lastName, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(ProjectTeamMemberResponse::firstName, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(ProjectTeamMemberResponse::startDate, Comparator.nullsLast(LocalDate::compareTo)))
                .toList();
    }

    @Override
    @Transactional
    public AllocationRequestResponse createAllocationRequest(CreateAllocationRequest request) {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        requireOwnedProject(caller, request.projectId());

        if (request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw badRequest("End date cannot be before start date");
        }

        if (request.specificEmployeeId() != null && !employeeRepository.existsById(request.specificEmployeeId())) {
            throw notFound("Employee not found with id " + request.specificEmployeeId());
        }

        Long creatorEmployeeId = employeeService.getEmployeeByAuthUserId(caller.userId()).id();

        String persistedComment = buildPersistedComment(request);
        AllocationRequest allocationRequest = AllocationRequest.builder()
                .createdByEmployeeId(creatorEmployeeId)
                .projectId(request.projectId())
                .comment(persistedComment)
                .build();

        AllocationRequest savedRequest = allocationRequestRepository.save(allocationRequest);

        if (request.specificEmployeeId() != null) {
            Engagement engagement = Engagement.builder()
                    .allocationRequestId(savedRequest.getId())
                    .employeeId(request.specificEmployeeId())
                    .startDate(request.startDate())
                    .endDate(request.endDate())
                    .engagementLevel(request.engagementLevel().trim().toUpperCase())
                    .roleOnProject(request.requiredRole().trim())
                    .status(EngagementStatus.PLANNED)
                    .createdBy(creatorEmployeeId)
                    .build();
            engagementRepository.save(engagement);
        }

        return toAllocationResponse(savedRequest, List.of(), List.of());
    }

    @Override
    public List<AllocationRequestResponse> listAllocationRequests(Long projectId) {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        requireRole(caller, "Only project managers can view allocation requests", ROLE_PROJECT_MANAGER);

        List<AllocationRequest> requests = fetchOwnedRequests(caller, projectId);
        if (requests.isEmpty()) {
            return List.of();
        }

        List<Long> requestIds = requests.stream().map(AllocationRequest::getId).toList();
        List<Engagement> engagements = engagementRepository.findByAllocationRequestIdIn(requestIds);

        Map<Long, List<Engagement>> engagementsByRequestId = engagements.stream()
                .collect(Collectors.groupingBy(Engagement::getAllocationRequestId));

        List<Long> engagementIds = engagements.stream().map(Engagement::getId).toList();
        List<EngagementDecision> decisions = engagementIds.isEmpty()
                ? List.of()
                : engagementDecisionRepository.findByEngagementIdIn(engagementIds);

        Map<Long, List<EngagementDecision>> decisionsByEngagementId = decisions.stream()
                .collect(Collectors.groupingBy(EngagementDecision::getEngagementId));

        return requests.stream()
                .map(request -> toAllocationResponse(
                        request,
                        engagementsByRequestId.getOrDefault(request.getId(), List.of()),
                        flattenDecisions(engagementsByRequestId.getOrDefault(request.getId(), List.of()), decisionsByEngagementId)))
                .toList();
    }

    private List<AllocationRequest> fetchOwnedRequests(CurrentUserContext caller, Long projectId) {
        if (projectId != null) {
            requireOwnedProject(caller, projectId);
            return allocationRequestRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
        }

        List<Long> ownedProjectIds = projectRepository.findBySponsorEmployeeId(caller.userId())
                .stream()
                .map(Project::getId)
                .toList();

        if (ownedProjectIds.isEmpty()) {
            return List.of();
        }

        return allocationRequestRepository.findByProjectIdInOrderByCreatedAtDesc(ownedProjectIds);
    }

    private List<EngagementDecision> flattenDecisions(
            List<Engagement> engagements,
            Map<Long, List<EngagementDecision>> decisionsByEngagementId) {
        List<EngagementDecision> decisions = new ArrayList<>();
        for (Engagement engagement : engagements) {
            decisions.addAll(decisionsByEngagementId.getOrDefault(engagement.getId(), List.of()));
        }
        return decisions;
    }

    private AllocationRequestResponse toAllocationResponse(
            AllocationRequest request,
            List<Engagement> engagements,
            List<EngagementDecision> decisions) {
        AllocationRequestParsedMetadata metadata = AllocationRequestMetadataParser.parse(request.getComment());

        String requiredRole = metadata.requiredRole();
        String engagementLevel = metadata.engagementLevel();
        LocalDate startDate = metadata.startDate();
        LocalDate endDate = metadata.endDate();
        Long specificEmployeeId = metadata.specificEmployeeId();

        if ((!metadata.hasStructuredData()) && !engagements.isEmpty()) {
            Engagement first = engagements.getFirst();
            requiredRole = first.getRoleOnProject();
            engagementLevel = first.getEngagementLevel();
            startDate = first.getStartDate();
            endDate = first.getEndDate();
            if (engagements.size() == 1) {
                specificEmployeeId = first.getEmployeeId();
            }
        }

        return staffingMapper.toAllocationRequestResponse(
            request,
                requiredRole,
                metadata.requiredSkill(),
                engagementLevel,
                startDate,
                endDate,
                metadata.userComment(),
                specificEmployeeId,
            resolveTrackingStatus(engagements, decisions));
    }

    private AllocationRequestTrackingStatus resolveTrackingStatus(
            List<Engagement> engagements,
            List<EngagementDecision> decisions) {
        if (engagements.isEmpty()) {
            return AllocationRequestTrackingStatus.PENDING;
        }

        boolean hasApproved = decisions.stream()
                .anyMatch(decision -> decision.getDecision() == EngagementDecisionType.APPROVED);
        if (hasApproved) {
            return AllocationRequestTrackingStatus.APPROVED;
        }

        boolean hasRejected = decisions.stream()
                .anyMatch(decision -> decision.getDecision() == EngagementDecisionType.REJECTED);
        if (hasRejected) {
            return AllocationRequestTrackingStatus.REJECTED;
        }

        return AllocationRequestTrackingStatus.PENDING;
    }

    private Map<Long, Employee> loadEmployeesById(Set<Long> employeeIds) {
        if (employeeIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Employee> byId = new HashMap<>();
        employeeRepository.findAllById(employeeIds)
                .forEach(employee -> byId.put(employee.getId(), employee));
        return byId;
    }

    private Project requireOwnedProject(CurrentUserContext caller, Long projectId) {
        requireRole(caller, "Only project managers can manage project staffing", ROLE_PROJECT_MANAGER);

        return projectRepository.findByIdAndSponsorEmployeeId(projectId, caller.userId())
                .orElseThrow(() -> notFound("Project not found with id " + projectId));
    }

    private void requireRole(CurrentUserContext caller, String message, String... roles) {
        if (caller == null || !caller.hasAnyRole(roles)) {
            throw forbidden(message);
        }
    }

    private String buildPersistedComment(CreateAllocationRequest request) {
        StringBuilder builder = new StringBuilder();

        if (request.comment() != null && !request.comment().isBlank()) {
            builder.append(request.comment().trim());
        }

        if (!builder.isEmpty()) {
            builder.append("\n\n");
        }

        builder.append(AllocationRequestMetadataParser.METADATA_MARKER)
                .append("\nrequiredRole=").append(request.requiredRole().trim())
                .append("\nrequiredSkill=").append(request.requiredSkill().trim())
                .append("\nengagementLevel=").append(request.engagementLevel().trim().toUpperCase())
                .append("\nstartDate=").append(request.startDate());

        if (request.endDate() != null) {
            builder.append("\nendDate=").append(request.endDate());
        }

        if (request.specificEmployeeId() != null) {
            builder.append("\nspecificEmployeeId=").append(request.specificEmployeeId());
        }

        String persistedComment = builder.toString();
        if (persistedComment.length() > 1000) {
            throw badRequest("Allocation request comment and metadata are too long");
        }

        return persistedComment;
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

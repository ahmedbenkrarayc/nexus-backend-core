package com.nexus.staffing.service.impl;

import com.nexus.employee.dto.response.absense.AbsenseResponse;
import com.nexus.employee.dto.response.employeeskill.EmployeeSkillResponse;
import com.nexus.employee.model.Absense;
import com.nexus.employee.model.Employee;
import com.nexus.employee.model.EmployeeSkill;
import com.nexus.employee.mapper.EmployeeSkillMapper;
import com.nexus.employee.repository.AbsenseRepository;
import com.nexus.employee.repository.EmployeeRepository;
import com.nexus.employee.repository.EmployeeSkillRepository;
import com.nexus.organization.model.Campus;
import com.nexus.organization.repository.CampusRepository;
import com.nexus.project.model.Project;
import com.nexus.project.repository.ProjectRepository;
import com.nexus.shared.security.context.CurrentUserContext;
import com.nexus.shared.security.provider.CurrentUserProvider;
import com.nexus.staffing.allocationrequest.AllocationRequestMetadataParser;
import com.nexus.staffing.allocationrequest.AllocationRequestParsedMetadata;
import com.nexus.staffing.dto.response.allocationrequest.AllocationRequestTrackingStatus;
import com.nexus.staffing.dto.response.employee.EmployeeAllocationRequestResponse;
import com.nexus.staffing.dto.response.employee.EmployeeEngagementResponse;
import com.nexus.staffing.dto.response.employee.EmployeeProjectDetailsResponse;
import com.nexus.staffing.mapper.EmployeeSelfServiceMapper;
import com.nexus.staffing.model.AllocationRequest;
import com.nexus.staffing.model.Engagement;
import com.nexus.staffing.model.EngagementDecision;
import com.nexus.staffing.model.enums.EngagementDecisionType;
import com.nexus.staffing.repository.AllocationRequestRepository;
import com.nexus.staffing.repository.EngagementDecisionRepository;
import com.nexus.staffing.repository.EngagementRepository;
import com.nexus.staffing.service.EmployeeSelfServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeSelfServiceServiceImpl implements EmployeeSelfServiceService {

    private final EngagementRepository engagementRepository;
    private final AllocationRequestRepository allocationRequestRepository;
    private final EngagementDecisionRepository engagementDecisionRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final AbsenseRepository absenseRepository;
    private final CampusRepository campusRepository;
    private final CurrentUserProvider currentUserProvider;
    private final EmployeeSelfServiceMapper mapper;
    private final EmployeeSkillMapper employeeSkillMapper;

    @Override
    public List<EmployeeEngagementResponse> getMyEngagements() {
        Employee me = getCurrentEmployee();
        List<Engagement> engagements = engagementRepository.findByEmployeeId(me.getId());
        
        return engagements.stream()
                .map(this::toEngagementResponse)
                .toList();
    }

    @Override
    public EmployeeProjectDetailsResponse getMyProjectDetails(Long projectId) {
        Employee me = getCurrentEmployee();
        
        // Find engagement to verify access
        Engagement engagement = engagementRepository.findByEmployeeId(me.getId()).stream()
                .filter(e -> isEngagementForProject(e, projectId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: you are not assigned to this project"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        Campus campus = campusRepository.findById(project.getOwnerCampusId()).orElse(null);
        String campusName = campus != null ? campus.getName() : "Unknown";

        EmployeeEngagementResponse engagementDto = toEngagementResponse(engagement);
        
        return mapper.toProjectDetailsResponse(project, campusName, engagementDto);
    }

    @Override
    public List<EmployeeAllocationRequestResponse> getMyAllocationRequests() {
        Employee me = getCurrentEmployee();
        
        // MVP logic: filter all requests where metadata.specificEmployeeId == me.id
        // For performance, we could use a custom query if needed
        List<AllocationRequest> allRequests = allocationRequestRepository.findAll();
        
        return allRequests.stream()
                .filter(req -> isTargetedAt(req, me.getId()))
                .map(this::toAllocationRequestResponse)
                .toList();
    }

    @Override
    public EmployeeAllocationRequestResponse getMyAllocationRequestDetails(Long requestId) {
        Employee me = getCurrentEmployee();
        AllocationRequest request = allocationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        if (!isTargetedAt(request, me.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: this request does not concern you");
        }

        return toAllocationRequestResponse(request);
    }

    @Override
    public List<EmployeeSkillResponse> getMySkills() {
        Employee me = getCurrentEmployee();
        List<EmployeeSkill> skills = employeeSkillRepository.findAll().stream()
                .filter(s -> s.getEmployee().getId().equals(me.getId()))
                .toList();
        
        return skills.stream()
                .map(employeeSkillMapper::toResponse)
                .toList();
    }

    @Override
    public List<AbsenseResponse> getMyAbsences() {
        Employee me = getCurrentEmployee();
        List<Absense> absences = absenseRepository.findByEmployeeIdIn(List.of(me.getId()));
        
        return absences.stream()
                .map(mapper::toAbsenceResponse)
                .toList();
    }

    private Employee getCurrentEmployee() {
        CurrentUserContext ctx = currentUserProvider.getCurrentUser();
        return employeeRepository.findByAuthUser(ctx.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Employee record not found for user"));
    }

    private boolean isEngagementForProject(Engagement engagement, Long projectId) {
        AllocationRequest req = allocationRequestRepository.findById(engagement.getAllocationRequestId()).orElse(null);
        return req != null && req.getProjectId().equals(projectId);
    }

    private boolean isTargetedAt(AllocationRequest request, Long employeeId) {
        AllocationRequestParsedMetadata metadata = AllocationRequestMetadataParser.parse(request.getComment());
        return employeeId.equals(metadata.specificEmployeeId());
    }

    private EmployeeEngagementResponse toEngagementResponse(Engagement engagement) {
        AllocationRequest req = allocationRequestRepository.findById(engagement.getAllocationRequestId()).orElse(null);
        Project project = req != null ? projectRepository.findById(req.getProjectId()).orElse(null) : null;
        Campus campus = project != null ? campusRepository.findById(project.getOwnerCampusId()).orElse(null) : null;
        String campusName = campus != null ? campus.getName() : "Unknown";
        
        return mapper.toEngagementResponse(engagement, project, campusName);
    }

    private EmployeeAllocationRequestResponse toAllocationRequestResponse(AllocationRequest request) {
        AllocationRequestParsedMetadata metadata = AllocationRequestMetadataParser.parse(request.getComment());
        Project project = projectRepository.findById(request.getProjectId()).orElse(null);
        Campus campus = project != null ? campusRepository.findById(project.getOwnerCampusId()).orElse(null) : null;
        String campusName = campus != null ? campus.getName() : "Unknown";
        
        Employee requester = employeeRepository.findById(request.getCreatedByEmployeeId()).orElse(null);
        
        // Determine status (reusing logic from ProjectManagerStaffingServiceImpl)
        List<Engagement> engagements = engagementRepository.findByAllocationRequestId(request.getId());
        List<Long> engagementIds = engagements.stream().map(Engagement::getId).toList();
        List<EngagementDecision> decisions = engagementIds.isEmpty() ? List.of() : engagementDecisionRepository.findByEngagementIdIn(engagementIds);
        
        AllocationRequestTrackingStatus status = resolveTrackingStatus(engagements, decisions);

        return mapper.toAllocationRequestResponse(
                request,
                project,
                campusName,
                requester,
                metadata.requiredRole(),
                metadata.engagementLevel(),
                metadata.startDate(),
                metadata.endDate(),
                metadata.userComment(),
                status
        );
    }

    private AllocationRequestTrackingStatus resolveTrackingStatus(List<Engagement> engagements, List<EngagementDecision> decisions) {
        if (engagements.isEmpty()) return AllocationRequestTrackingStatus.PENDING;
        
        boolean hasApproved = decisions.stream().anyMatch(d -> d.getDecision() == EngagementDecisionType.APPROVED);
        if (hasApproved) return AllocationRequestTrackingStatus.APPROVED;
        
        boolean hasRejected = decisions.stream().anyMatch(d -> d.getDecision() == EngagementDecisionType.REJECTED);
        if (hasRejected) return AllocationRequestTrackingStatus.REJECTED;
        
        return AllocationRequestTrackingStatus.PENDING;
    }
}

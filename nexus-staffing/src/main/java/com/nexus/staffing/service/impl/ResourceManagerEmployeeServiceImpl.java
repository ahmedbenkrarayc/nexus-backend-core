package com.nexus.staffing.service.impl;

import com.nexus.employee.model.Absense;
import com.nexus.employee.model.Employee;
import com.nexus.employee.model.EmployeeResponsibility;
import com.nexus.employee.model.EmployeeSkill;
import com.nexus.employee.repository.AbsenseRepository;
import com.nexus.employee.repository.EmployeeRepository;
import com.nexus.employee.repository.EmployeeResponsibilityRepository;
import com.nexus.employee.repository.EmployeeSkillRepository;
import com.nexus.project.model.Project;
import com.nexus.project.repository.ProjectRepository;
import com.nexus.shared.security.context.CurrentUserContext;
import com.nexus.shared.security.provider.CurrentUserProvider;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerEmployeeAbsenceResponse;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerEmployeeDetailsResponse;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerEmployeeEngagementResponse;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerEmployeeListItemResponse;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerEmployeeSkillResponse;
import com.nexus.staffing.mapper.ResourceManagerStaffingMapper;
import com.nexus.staffing.model.AllocationRequest;
import com.nexus.staffing.model.Engagement;
import com.nexus.staffing.model.enums.EngagementStatus;
import com.nexus.staffing.repository.AllocationRequestRepository;
import com.nexus.staffing.repository.EngagementRepository;
import com.nexus.staffing.service.ResourceManagerEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResourceManagerEmployeeServiceImpl implements ResourceManagerEmployeeService {

    private static final String ROLE_RESOURCE_MANAGER = "RESOURCE_MANAGER";
    private static final String RESPONSIBILITY_TYPE_RESOURCE_MANAGER = "RESOURCE_MANAGER";

    private final CurrentUserProvider currentUserProvider;
    private final EmployeeRepository employeeRepository;
    private final EmployeeResponsibilityRepository responsibilityRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final AbsenseRepository absenseRepository;
    private final EngagementRepository engagementRepository;
    private final AllocationRequestRepository allocationRequestRepository;
    private final ProjectRepository projectRepository;
    private final ResourceManagerStaffingMapper mapper;

    @Override
    public List<ResourceManagerEmployeeListItemResponse> listManagedEmployees() {
        Employee resourceManager = requireCurrentResourceManagerEmployee();

        return responsibilityRepository
                .findAllByResponsibleIdAndTypeAndActiveTrueWithEmployee(resourceManager.getId(),
                        RESPONSIBILITY_TYPE_RESOURCE_MANAGER)
                .stream()
                .map(EmployeeResponsibility::getEmployee)
                .distinct()
                .sorted(Comparator
                        .comparing(Employee::getLname, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(Employee::getFname, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(mapper::toEmployeeListItem)
                .toList();
    }

    @Override
    public ResourceManagerEmployeeDetailsResponse getManagedEmployeeDetails(Long employeeId) {
        Employee resourceManager = requireCurrentResourceManagerEmployee();
        Employee employee = requireManagedEmployee(resourceManager.getId(), employeeId);

        List<ResourceManagerEmployeeSkillResponse> skills = employeeSkillRepository
                .findAllByEmployeeIdOrderByIdAsc(employeeId)
                .stream()
                .map(mapper::toEmployeeSkillResponse)
                .toList();

        List<ResourceManagerEmployeeAbsenceResponse> absences = absenseRepository.findByEmployeeId(employeeId)
                .stream()
                .sorted(Comparator.comparing(Absense::getStart).reversed())
                .map(mapper::toEmployeeAbsenceResponse)
                .toList();

        List<ResourceManagerEmployeeEngagementResponse> currentEngagements = buildEngagementResponses(employeeId, true);

        return new ResourceManagerEmployeeDetailsResponse(
                employee.getId(),
                employee.getCode(),
                employee.getFname(),
                employee.getLname(),
                employee.getEmail(),
                employee.getCampusId(),
                skills,
                absences,
                currentEngagements);
    }

    @Override
    public List<ResourceManagerEmployeeEngagementResponse> listCurrentEngagements(Long employeeId) {
        Employee resourceManager = requireCurrentResourceManagerEmployee();
        requireManagedEmployee(resourceManager.getId(), employeeId);

        return buildEngagementResponses(employeeId, true);
    }

    @Override
    public List<ResourceManagerEmployeeAbsenceResponse> listEmployeeAbsences(Long employeeId) {
        Employee resourceManager = requireCurrentResourceManagerEmployee();
        requireManagedEmployee(resourceManager.getId(), employeeId);

        return absenseRepository.findByEmployeeId(employeeId)
                .stream()
                .sorted(Comparator.comparing(Absense::getStart).reversed())
                .map(mapper::toEmployeeAbsenceResponse)
                .toList();
    }

    private List<ResourceManagerEmployeeEngagementResponse> buildEngagementResponses(Long employeeId,
            boolean onlyCurrent) {
        List<Engagement> engagements = engagementRepository.findByEmployeeId(employeeId);

        if (onlyCurrent) {
            engagements = engagements.stream()
                    .filter(engagement -> engagement.getStatus() == EngagementStatus.PLANNED
                            || engagement.getStatus() == EngagementStatus.ACTIVE)
                    .toList();
        }

        if (engagements.isEmpty()) {
            return List.of();
        }

        List<Long> requestIds = engagements.stream().map(Engagement::getAllocationRequestId).distinct().toList();
        Map<Long, AllocationRequest> requestById = allocationRequestRepository
                .findByIdInOrderByCreatedAtDesc(requestIds)
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
                .sorted(Comparator.comparing(Engagement::getStartDate).reversed())
                .map(engagement -> {
                    AllocationRequest request = requestById.get(engagement.getAllocationRequestId());
                    Project project = request == null ? null : projectById.get(request.getProjectId());
                    Long projectId = project == null ? null : project.getId();
                    String projectName = project == null ? null : project.getName();
                    return mapper.toEmployeeEngagementResponse(engagement, projectId, projectName);
                })
                .toList();
    }

    private Employee requireManagedEmployee(Long resourceManagerId, Long employeeId) {
        Map<Long, Employee> managedEmployees = managedEmployeesById(resourceManagerId);
        Employee employee = managedEmployees.get(employeeId);
        if (employee == null) {
            throw forbidden("You are not allowed to access this employee");
        }
        return employee;
    }

    private Map<Long, Employee> managedEmployeesById(Long resourceManagerId) {
        Map<Long, Employee> managed = new HashMap<>();
        for (EmployeeResponsibility responsibility : managedResponsibilities(resourceManagerId)) {
            managed.put(responsibility.getEmployee().getId(), responsibility.getEmployee());
        }
        return managed;
    }

    private List<EmployeeResponsibility> managedResponsibilities(Long resourceManagerId) {
        return responsibilityRepository
                .findAllByResponsibleIdAndTypeAndActiveTrueOrderByStartDateDesc(
                        resourceManagerId,
                        RESPONSIBILITY_TYPE_RESOURCE_MANAGER);
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
}

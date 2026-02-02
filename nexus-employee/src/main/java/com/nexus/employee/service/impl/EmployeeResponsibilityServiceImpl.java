package com.nexus.employee.service.impl;

import com.nexus.employee.client.AuthServiceClient;
import com.nexus.employee.dto.request.responsibility.AssignResourceManagerRequest;
import com.nexus.employee.dto.request.responsibility.ChangeResourceManagerRequest;
import com.nexus.employee.dto.response.responsibility.EmployeeUnderResourceManagerResponse;
import com.nexus.employee.dto.response.responsibility.ResourceManagerResponsibilityResponse;
import com.nexus.employee.exception.EmployeeConflictException;
import com.nexus.employee.exception.ResourceNotFoundException;
import com.nexus.employee.mapper.EmployeeResponsibilityMapper;
import com.nexus.employee.model.Employee;
import com.nexus.employee.model.EmployeeResponsibility;
import com.nexus.employee.repository.EmployeeRepository;
import com.nexus.employee.repository.EmployeeResponsibilityRepository;
import com.nexus.employee.service.EmployeeResponsibilityService;
import com.nexus.shared.security.context.CurrentUserContext;
import com.nexus.shared.security.provider.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeResponsibilityServiceImpl implements EmployeeResponsibilityService {

    private static final String RM_TYPE = "RESOURCE_MANAGER";
    private static final String RM_ROLE = "RESOURCE_MANAGER";
    private static final String ROLE_HR_MANAGER = "HR_MANAGER";

    private final EmployeeRepository employeeRepository;
    private final EmployeeResponsibilityRepository responsibilityRepository;
    private final EmployeeResponsibilityMapper responsibilityMapper;
    private final AuthServiceClient authServiceClient;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public ResourceManagerResponsibilityResponse assignResourceManager(Long employeeId, AssignResourceManagerRequest request) {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        Employee currentEmployee = requireAuthorizedUser(caller);

        Employee employee = getEmployee(employeeId);
        Employee resourceManager = getEmployee(request.resourceManagerId());

        validateSameCampus(currentEmployee, employee, "assign resource manager for this employee");
        validateSameCampus(currentEmployee, resourceManager, "assign this resource manager");

        validateNotSelfManaged(employee.getId(), resourceManager.getId());
        validateManagedEmployeeIsNotResourceManager(employee);
        validateSelectedResourceManagerHasRole(resourceManager);

        EmployeeResponsibility current = responsibilityRepository
                .findFirstByEmployeeIdAndTypeAndActiveTrue(employeeId, RM_TYPE)
                .orElse(null);

        if (current != null) {
            if (current.getResponsible().getId().equals(resourceManager.getId())) {
                throw new EmployeeConflictException("Employee already assigned to this resource manager");
            }
            throw new EmployeeConflictException("Employee already has an active resource manager; use change operation");
        }

        EmployeeResponsibility created = responsibilityRepository.save(buildNewResponsibility(employee, resourceManager));
        return responsibilityMapper.toResourceManagerResponse(created);
    }

    @Override
    @Transactional
    public ResourceManagerResponsibilityResponse changeResourceManager(Long employeeId, ChangeResourceManagerRequest request) {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        Employee currentEmployee = requireAuthorizedUser(caller);

        Employee employee = getEmployee(employeeId);
        Employee newResourceManager = getEmployee(request.newResourceManagerId());

        validateSameCampus(currentEmployee, employee, "change resource manager for this employee");
        validateSameCampus(currentEmployee, newResourceManager, "assign this resource manager");

        validateNotSelfManaged(employee.getId(), newResourceManager.getId());
        validateManagedEmployeeIsNotResourceManager(employee);
        validateSelectedResourceManagerHasRole(newResourceManager);

        EmployeeResponsibility current = responsibilityRepository
                .findFirstByEmployeeIdAndTypeAndActiveTrue(employeeId, RM_TYPE)
                .orElseThrow(() -> new ResourceNotFoundException("No active resource manager responsibility found for employee " + employeeId));

        if (current.getResponsible().getId().equals(newResourceManager.getId())) {
            throw new EmployeeConflictException("Employee is already assigned to this resource manager");
        }

        current.setActive(false);
        current.setEndDate(LocalDateTime.now());
        responsibilityRepository.save(current);

        EmployeeResponsibility created = responsibilityRepository.save(buildNewResponsibility(employee, newResourceManager));
        return responsibilityMapper.toResourceManagerResponse(created);
    }

    @Override
    public ResourceManagerResponsibilityResponse getCurrentResourceManagerResponsibility(Long employeeId) {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        Employee currentEmployee = requireAuthorizedUser(caller);

        Employee employee = getEmployee(employeeId);
        validateSameCampus(currentEmployee, employee, "view this employee responsibility");

        EmployeeResponsibility responsibility = responsibilityRepository
                .findFirstByEmployeeIdAndTypeAndActiveTrue(employeeId, RM_TYPE)
                .orElseThrow(() -> new ResourceNotFoundException("No active resource manager responsibility found for employee " + employeeId));
        return responsibilityMapper.toResourceManagerResponse(responsibility);
    }

    @Override
    public List<EmployeeUnderResourceManagerResponse> listEmployeesUnderResourceManager(Long resourceManagerId) {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        Employee currentEmployee = requireAuthorizedUser(caller);

        Employee resourceManager = getEmployee(resourceManagerId);
        validateSameCampus(currentEmployee, resourceManager, "view employees under this resource manager");

        return responsibilityRepository.findAllByResponsibleIdAndTypeAndActiveTrueOrderByStartDateDesc(resourceManagerId, RM_TYPE)
                .stream()
                .map(responsibilityMapper::toEmployeeUnderResourceManagerResponse)
                .toList();
    }

    private static final String ROLE_ADMIN = "ADMIN";

    private Employee requireAuthorizedUser(CurrentUserContext caller) {
        if (caller == null || caller.userId() == null) {
            throw forbidden("Only HR manager or Admin can manage employee responsibilities");
        }

        if (caller.hasRole(ROLE_ADMIN)) {
            return employeeRepository.findByAuthUser(caller.userId()).orElse(null);
        }

        if (caller.hasRole(ROLE_HR_MANAGER)) {
            return employeeRepository.findByAuthUser(caller.userId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee profile not found for authenticated HR manager " + caller.userId()));
        }

        throw forbidden("Only HR manager or Admin can manage employee responsibilities");
    }

    private void validateSameCampus(Employee currentEmployee, Employee targetEmployee, String action) {
        if (currentEmployee == null) {
            return; // Admin without a specific employee profile bypasses campus check
        }
        if (!currentEmployee.getCampusId().equals(targetEmployee.getCampusId())) {
            throw forbidden("You are not allowed to " + action + " outside your campus");
        }
    }

    private Employee getEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id " + employeeId));
    }

    private void validateNotSelfManaged(Long employeeId, Long resourceManagerId) {
        if (employeeId.equals(resourceManagerId)) {
            throw new EmployeeConflictException("Employee cannot be their own resource manager");
        }
    }

    private void validateManagedEmployeeIsNotResourceManager(Employee employee) {
        boolean hasResourceManagerRole = authServiceClient.userHasRole(employee.getAuthUser(), RM_ROLE);
        if (hasResourceManagerRole) {
            throw new EmployeeConflictException("Managed employee cannot be assigned because they are a resource manager");
        }
    }

    private void validateSelectedResourceManagerHasRole(Employee resourceManager) {
        boolean hasResourceManagerRole = authServiceClient.userHasRole(resourceManager.getAuthUser(), RM_ROLE);
        if (!hasResourceManagerRole) {
            throw new EmployeeConflictException("Selected resource manager does not have RESOURCE_MANAGER role");
        }
    }

    private EmployeeResponsibility buildNewResponsibility(Employee employee, Employee resourceManager) {
        return EmployeeResponsibility.builder()
                .employee(employee)
                .responsible(resourceManager)
                .type(RM_TYPE)
                .active(true)
                .startDate(LocalDateTime.now())
                .endDate(null)
                .build();
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }
}
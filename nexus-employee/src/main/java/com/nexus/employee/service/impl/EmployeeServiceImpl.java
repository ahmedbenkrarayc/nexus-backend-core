package com.nexus.employee.service.impl;

import com.nexus.employee.client.AuthServiceClient;
import com.nexus.employee.dto.request.employee.CreateEmployeeRequest;
import com.nexus.employee.dto.request.employee.UpdateEmployeeInfoRequest;
import com.nexus.employee.dto.response.employee.EmployeePageResponse;
import com.nexus.employee.dto.response.employee.EmployeeResponse;
import com.nexus.employee.exception.EmployeeConflictException;
import com.nexus.employee.exception.EmployeeProvisioningException;
import com.nexus.employee.exception.ExternalServiceException;
import com.nexus.employee.exception.ResourceNotFoundException;
import com.nexus.employee.mapper.EmployeeMapper;
import com.nexus.employee.model.Employee;
import com.nexus.employee.repository.EmployeeRepository;
import com.nexus.employee.service.CampusLookupService;
import com.nexus.employee.service.EmployeeService;
import com.nexus.shared.security.context.CurrentUserContext;
import com.nexus.shared.security.provider.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_HR_MANAGER = "HR_MANAGER";
    private static final String ROLE_PROJECT_MANAGER = "PROJECT_MANAGER";
    private static final String ROLE_RESOURCE_MANAGER = "RESOURCE_MANAGER";
    private static final String ROLE_EMPLOYEE = "EMPLOYEE";

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final AuthServiceClient authServiceClient;
    private final CampusLookupService campusLookupService;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        requireRole(caller, "Only admin can create employees", ROLE_ADMIN);
        validateNewEmployee(request);

        Long createdUserId = null;
        try {
            validateRequestedRoles(request.user().roles());
            createdUserId = authServiceClient.registerUser(request.user());

            Employee employee = employeeMapper.toEmployee(request.employee());
            employee.setAuthUser(createdUserId);
            employee.setEmail(normalizeEmail(request.user().email()));

            return employeeMapper.toResponse(employeeRepository.save(employee));
        } catch (RuntimeException exception) {
            compensateCreatedUser(createdUserId, exception);
            throw exception;
        }
    }

    @Override
    public EmployeePageResponse listEmployeesPage(String search, String role, int page, int size) {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Long campusScope = resolveCampusScopeForList(caller);
        String normalizedSearch = normalizeSearch(search);

        java.util.List<Long> authUserIds = null;
        if (role != null && !role.isBlank()) {
            authUserIds = authServiceClient.getUserIdsByRole(role.trim());
            if (authUserIds.isEmpty()) {
                // Short-circuit: Role provided, but no users found.
                return new EmployeePageResponse(java.util.List.of(), page, size, 0, 0, true, true);
            }
        }

        Page<Employee> employeePage;

        if (normalizedSearch == null) {
            if (authUserIds == null) {
                employeePage = campusScope == null
                        ? employeeRepository.findAll(pageable)
                        : employeeRepository.findByCampusId(campusScope, pageable);
            } else {
                employeePage = campusScope == null
                        ? employeeRepository.findByAuthUserIn(authUserIds, pageable)
                        : employeeRepository.findByCampusIdAndAuthUserIn(campusScope, authUserIds, pageable);
            }
        } else {
            String searchPattern = "%" + normalizedSearch.toLowerCase(Locale.ROOT) + "%";
            if (authUserIds == null) {
                employeePage = employeeRepository.searchEmployees(campusScope, searchPattern, pageable);
            } else {
                employeePage = employeeRepository.searchEmployeesInAuthUsers(campusScope, authUserIds, searchPattern, pageable);
            }
        }

        return new EmployeePageResponse(
                employeePage.getContent().stream().map(employeeMapper::toListItem).toList(),
                employeePage.getNumber(),
                employeePage.getSize(),
                employeePage.getTotalElements(),
                employeePage.getTotalPages(),
                employeePage.isFirst(),
                employeePage.isLast());
    }

    @Override
    public EmployeeResponse getEmployeeDetails(Long employeeId) {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        Employee targetEmployee = getEmployee(employeeId);

        if (!canViewEmployee(caller, targetEmployee)) {
            throw forbidden("You are not allowed to view this employee details");
        }

        return employeeMapper.toResponse(targetEmployee);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployeeInfo(Long employeeId, UpdateEmployeeInfoRequest request) {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        Employee employee = getEmployee(employeeId);
        validateCanUpdateEmployee(caller, employee);
        campusLookupService.ensureCampusExists(request.campusId());

        if (employeeRepository.existsByCodeIgnoreCaseAndIdNot(request.code(), employeeId)) {
            throw new EmployeeConflictException("Employee code already exists");
        }

        employeeMapper.updateEmployee(request, employee);
        return employeeMapper.toResponse(employeeRepository.save(employee));
    }

    @Override
    public EmployeeResponse getEmployeeByAuthUserId(Long authUserId) {
        Employee employee = employeeRepository.findByAuthUser(authUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for auth user " + authUserId));
        return employeeMapper.toResponse(employee);
    }

    private Employee getEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id " + employeeId));
    }

    private void validateNewEmployee(CreateEmployeeRequest request) {
        campusLookupService.ensureCampusExists(request.employee().campusId());

        if (employeeRepository.existsByCodeIgnoreCase(request.employee().code())) {
            throw new EmployeeConflictException("Employee code already exists");
        }

        String email = normalizeEmail(request.user().email());
        if (employeeRepository.existsByEmailIgnoreCase(email)) {
            throw new EmployeeConflictException("Employee email already exists");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeSearch(String search) {
        if (search == null) {
            return null;
        }

        String normalized = search.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void validateCanUpdateEmployee(CurrentUserContext caller, Employee targetEmployee) {
        if (caller.hasRole(ROLE_ADMIN)) {
            return;
        }

        if (caller.hasAnyRole(ROLE_HR_MANAGER)) {
            Employee currentEmployee = getCurrentEmployee(caller);
            if (currentEmployee.getCampusId().equals(targetEmployee.getCampusId())) {
                return;
            }
        }

        throw forbidden("You are not allowed to update this employee");
    }

    private boolean canViewEmployee(CurrentUserContext caller, Employee targetEmployee) {
        if (caller.hasAnyRole(ROLE_ADMIN, ROLE_PROJECT_MANAGER)) {
            return true;
        }

        if (caller.hasAnyRole(ROLE_HR_MANAGER, ROLE_RESOURCE_MANAGER)) {
            Employee currentEmployee = getCurrentEmployee(caller);
            return currentEmployee.getCampusId().equals(targetEmployee.getCampusId());
        }

        return caller.hasRole(ROLE_EMPLOYEE)
                && caller.userId() != null
                && caller.userId().equals(targetEmployee.getAuthUser());
    }

    private Long resolveCampusScopeForList(CurrentUserContext caller) {
        if (caller.hasRole(ROLE_ADMIN)) {
            return null;
        }

        if (caller.hasAnyRole(ROLE_HR_MANAGER)) {
            return getCurrentEmployee(caller).getCampusId();
        }

        throw forbidden("You are not allowed to list employees");
    }

    private Employee getCurrentEmployee(CurrentUserContext caller) {
        if (caller == null || caller.userId() == null) {
            throw forbidden("Authenticated user is required");
        }

        return employeeRepository.findByAuthUser(caller.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee profile not found for authenticated user " + caller.userId()));
    }

    private void requireRole(CurrentUserContext caller, String message, String... roles) {
        if (caller == null || !caller.hasAnyRole(roles)) {
            throw forbidden(message);
        }
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }

    private void validateRequestedRoles(Set<String> roles) {
        Set<String> normalized = roles == null
                ? Set.of()
                : roles.stream()
                        .filter(role -> role != null && !role.isBlank())
                        .map(String::trim)
                        .collect(Collectors.toCollection(HashSet::new));

        if (normalized.isEmpty()) {
            throw new EmployeeConflictException("At least one role is required");
        }
    }

    private void compensateCreatedUser(Long createdUserId, RuntimeException rootException) {
        if (createdUserId == null) {
            return;
        }

        try {
            authServiceClient.deactivateUser(createdUserId);
        } catch (ExternalServiceException compensationException) {
            throw new EmployeeProvisioningException(
                    "Employee creation failed and user compensation also failed. User id "
                            + createdUserId
                            + " should be deactivated manually",
                    compensationException);
        }

        if (rootException instanceof EmployeeConflictException
                || rootException instanceof ResourceNotFoundException
                || rootException instanceof ExternalServiceException
                || rootException instanceof EmployeeProvisioningException) {
            throw rootException;
        }

        throw new EmployeeProvisioningException(
                "Employee creation failed after user provisioning; user was deactivated",
                rootException);
    }

}
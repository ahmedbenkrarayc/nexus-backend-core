package com.nexus.employee.service.impl;

import com.nexus.employee.dto.request.absense.CreateAbsenseRequest;
import com.nexus.employee.dto.request.absense.UpdateAbsenseRequest;
import com.nexus.employee.dto.response.absense.AbsenseResponse;
import com.nexus.employee.dto.response.absense.AbsensePageResponse;
import com.nexus.employee.exception.ResourceNotFoundException;
import com.nexus.employee.exception.EmployeeConflictException;
import com.nexus.employee.mapper.AbsenseMapper;
import com.nexus.employee.model.Absense;
import com.nexus.employee.model.Employee;
import com.nexus.employee.repository.AbsenseRepository;
import com.nexus.employee.repository.EmployeeRepository;
import com.nexus.employee.service.AbsenseService;
import com.nexus.shared.security.context.CurrentUserContext;
import com.nexus.shared.security.provider.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AbsenseServiceImpl implements AbsenseService {

    private final AbsenseRepository absenseRepository;
    private final EmployeeRepository employeeRepository;
    private final AbsenseMapper absenseMapper;
    private final CurrentUserProvider currentUserProvider;

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_HR_MANAGER = "HR_MANAGER";
    private static final String ROLE_EMPLOYEE = "EMPLOYEE";

    @Override
    @Transactional
    public AbsenseResponse createAbsense(CreateAbsenseRequest request) {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        requireRole(caller, "Only HR Manager can create absences", ROLE_HR_MANAGER);

        Employee targetEmployee = getEmployee(request.employeeId());
        validateCanManageEmployee(caller, targetEmployee, "create absence");

        validateDateRange(request.start(), request.end());

        Absense absense = absenseMapper.toAbsense(request);
        absense.setEmployee(targetEmployee);
        absense.setApproved(false);

        Absense savedAbsense = absenseRepository.save(absense);
        log.info("Absence created for employee {} by {} ", targetEmployee.getId(), caller.userId());

        return absenseMapper.toResponse(savedAbsense);
    }

    @Override
    public AbsenseResponse getAbsenseDetails(Long absenseId) {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        Absense absense = getAbsense(absenseId);

        validateCanViewAbsense(caller, absense);

        return absenseMapper.toResponse(absense);
    }

    @Override
    @Transactional
    public AbsenseResponse updateAbsense(Long absenseId, UpdateAbsenseRequest request) {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        requireRole(caller, "Only HR Manager can update absences", ROLE_HR_MANAGER);

        Absense absense = getAbsense(absenseId);
        validateCanManageEmployee(caller, absense.getEmployee(), "update absence");

        validateDateRange(request.start(), request.end());

        absenseMapper.updateAbsense(request, absense);
        Absense updatedAbsense = absenseRepository.save(absense);

        log.info("Absence {} updated by {}", absenseId, caller.userId());

        return absenseMapper.toResponse(updatedAbsense);
    }

    @Override
    @Transactional
    public void deleteAbsense(Long absenseId) {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        requireRole(caller, "Only HR Manager can delete absences", ROLE_HR_MANAGER);

        Absense absense = getAbsense(absenseId);
        validateCanManageEmployee(caller, absense.getEmployee(), "delete absence");

        absenseRepository.delete(absense);
        log.info("Absence {} deleted by {}", absenseId, caller.userId());
    }

    @Override
    public AbsensePageResponse listAbsensesByEmployee(Long employeeId, int page, int size) {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        Employee employee = getEmployee(employeeId);

        validateCanViewEmployee(caller, employee);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Absense> absensePage = absenseRepository.findByEmployeeId(employeeId, pageable);

        return buildPageResponse(absensePage);
    }

    @Override
    public AbsensePageResponse listAllAbsenses(int page, int size, String search) {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        requireRole(caller, "Only HR Manager can list all absences", ROLE_HR_MANAGER);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Employee currentEmployee = getCurrentEmployee(caller);
        Long campusScope = currentEmployee.getCampusId();

        Page<Absense> absensePage;

        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = search.toLowerCase(Locale.ROOT);
            absensePage = absenseRepository.searchAbsensesByCampus(campusScope, searchPattern, pageable);
        } else {
            absensePage = absenseRepository.findByCampusId(campusScope, pageable);
        }

        return buildPageResponse(absensePage);
    }

    // Helper methods

    private Absense getAbsense(Long absenseId) {
        return absenseRepository.findById(absenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Absence not found with id " + absenseId));
    }

    private Employee getEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id " + employeeId));
    }

    private Employee getCurrentEmployee(CurrentUserContext caller) {
        return employeeRepository.findByAuthUser(caller.userId())
                .orElseThrow(() -> new EmployeeConflictException("Current user is not an employee"));
    }

    private void requireRole(CurrentUserContext caller, String message, String... roles) {
        if (caller == null || !caller.hasAnyRole(roles)) {
            throw forbidden(message);
        }
    }

    private void validateCanManageEmployee(CurrentUserContext caller, Employee targetEmployee, String action) {
        if (caller.hasRole(ROLE_ADMIN)) {
            return;
        }

        if (caller.hasRole(ROLE_HR_MANAGER)) {
            Employee currentEmployee = getCurrentEmployee(caller);
            if (currentEmployee.getCampusId().equals(targetEmployee.getCampusId())) {
                return;
            }
        }

        throw forbidden("You are not allowed to " + action + " for this employee. Employee is in a different campus.");
    }

    private void validateCanViewAbsense(CurrentUserContext caller, Absense absense) {
        if (caller.hasRole(ROLE_ADMIN)) {
            return;
        }

        if (caller.hasRole(ROLE_HR_MANAGER)) {
            Employee currentEmployee = getCurrentEmployee(caller);
            if (currentEmployee.getCampusId().equals(absense.getEmployee().getCampusId())) {
                return;
            }
            throw forbidden("You are not allowed to view this absence");
        }

        if (caller.hasRole(ROLE_EMPLOYEE)) {
            if (caller.userId() != null && caller.userId().equals(absense.getEmployee().getAuthUser())) {
                return;
            }
            throw forbidden("You can only view your own absences");
        }

        throw forbidden("You are not allowed to view this absence");
    }

    private void validateCanViewEmployee(CurrentUserContext caller, Employee targetEmployee) {
        if (caller.hasRole(ROLE_ADMIN)) {
            return;
        }

        if (caller.hasRole(ROLE_HR_MANAGER)) {
            Employee currentEmployee = getCurrentEmployee(caller);
            if (currentEmployee.getCampusId().equals(targetEmployee.getCampusId())) {
                return;
            }
            throw forbidden("You can only view employees from your campus");
        }

        if (caller.hasRole(ROLE_EMPLOYEE)) {
            if (caller.userId() != null && caller.userId().equals(targetEmployee.getAuthUser())) {
                return;
            }
            throw forbidden("You can only view your own absences");
        }

        throw forbidden("You are not allowed to view this employee's absences");
    }

    private void validateDateRange(java.time.LocalDate start, java.time.LocalDate end) {
        if (start.isAfter(end)) {
            throw new EmployeeConflictException("Start date cannot be after end date");
        }
    }

    private AbsensePageResponse buildPageResponse(Page<Absense> page) {
        return new AbsensePageResponse(
                page.getContent().stream()
                        .map(absenseMapper::toResponse)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }
}

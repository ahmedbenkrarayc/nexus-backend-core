package com.nexus.employee.controller;

import com.nexus.employee.dto.request.employee.CreateEmployeeRequest;
import com.nexus.employee.dto.request.employee.UpdateEmployeeInfoRequest;
import com.nexus.employee.dto.response.employee.EmployeePageResponse;
import com.nexus.employee.dto.response.employee.EmployeeResponse;
import com.nexus.employee.service.EmployeeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        return employeeService.createEmployee(request);
    }

    @GetMapping
    public EmployeePageResponse listEmployeesPage(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return employeeService.listEmployeesPage(search, role, page, size);
    }

    @GetMapping("/{employeeId}")
    public EmployeeResponse getEmployeeDetails(@PathVariable Long employeeId) {
        return employeeService.getEmployeeDetails(employeeId);
    }

    @PutMapping("/{employeeId}")
    public EmployeeResponse updateEmployeeInfo(
            @PathVariable Long employeeId,
            @Valid @RequestBody UpdateEmployeeInfoRequest request) {
        return employeeService.updateEmployeeInfo(employeeId, request);
    }

    @GetMapping("/auth-user/{authUserId}")
    public EmployeeResponse getEmployeeByAuthUserId(@PathVariable Long authUserId) {
        return employeeService.getEmployeeByAuthUserId(authUserId);
    }
}
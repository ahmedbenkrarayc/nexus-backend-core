package com.nexus.employee.controller;

import com.nexus.employee.dto.request.responsibility.AssignResourceManagerRequest;
import com.nexus.employee.dto.request.responsibility.ChangeResourceManagerRequest;
import com.nexus.employee.dto.response.responsibility.EmployeeUnderResourceManagerResponse;
import com.nexus.employee.dto.response.responsibility.ResourceManagerResponsibilityResponse;
import com.nexus.employee.service.EmployeeResponsibilityService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
public class EmployeeResponsibilityController {

    private final EmployeeResponsibilityService responsibilityService;

    @PostMapping("/{employeeId}/resource-manager")
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceManagerResponsibilityResponse assignResourceManager(
            @PathVariable @Positive(message = "Employee id must be positive") Long employeeId,
            @Valid @RequestBody AssignResourceManagerRequest request) {
        return responsibilityService.assignResourceManager(employeeId, request);
    }

    @PutMapping("/{employeeId}/resource-manager")
    public ResourceManagerResponsibilityResponse changeResourceManager(
            @PathVariable @Positive(message = "Employee id must be positive") Long employeeId,
            @Valid @RequestBody ChangeResourceManagerRequest request) {
        return responsibilityService.changeResourceManager(employeeId, request);
    }

    @GetMapping("/{employeeId}/resource-manager")
    public ResourceManagerResponsibilityResponse viewCurrentResourceManagerResponsibility(
            @PathVariable @Positive(message = "Employee id must be positive") Long employeeId) {
        return responsibilityService.getCurrentResourceManagerResponsibility(employeeId);
    }

    @GetMapping("/resource-managers/{resourceManagerId}/employees")
    public List<EmployeeUnderResourceManagerResponse> viewEmployeesUnderResourceManager(
            @PathVariable @Positive(message = "Resource manager id must be positive") Long resourceManagerId) {
        return responsibilityService.listEmployeesUnderResourceManager(resourceManagerId);
    }
}
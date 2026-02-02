package com.nexus.staffing.controller;

import com.nexus.staffing.dto.request.resourcemanager.ReviewAllocationRequestDecision;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerAllocationRequestDetailsResponse;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerAllocationRequestListItemResponse;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerEmployeeAbsenceResponse;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerEmployeeDetailsResponse;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerEmployeeEngagementResponse;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerEmployeeListItemResponse;
import com.nexus.staffing.service.ResourceManagerEmployeeService;
import com.nexus.staffing.service.ResourceManagerRequestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/staffing/resource-manager")
public class ResourceManagerStaffingController {

    private final ResourceManagerEmployeeService resourceManagerEmployeeService;
    private final ResourceManagerRequestService resourceManagerRequestService;

    @GetMapping("/employees")
    public List<ResourceManagerEmployeeListItemResponse> listManagedEmployees() {
        return resourceManagerEmployeeService.listManagedEmployees();
    }

    @GetMapping("/employees/{employeeId}")
    public ResourceManagerEmployeeDetailsResponse getManagedEmployeeDetails(
            @PathVariable @Positive(message = "Employee id must be positive") Long employeeId) {
        return resourceManagerEmployeeService.getManagedEmployeeDetails(employeeId);
    }

    @GetMapping("/employees/{employeeId}/engagements")
    public List<ResourceManagerEmployeeEngagementResponse> listCurrentEmployeeEngagements(
            @PathVariable @Positive(message = "Employee id must be positive") Long employeeId) {
        return resourceManagerEmployeeService.listCurrentEngagements(employeeId);
    }

    @GetMapping("/employees/{employeeId}/absences")
    public List<ResourceManagerEmployeeAbsenceResponse> listEmployeeAbsences(
            @PathVariable @Positive(message = "Employee id must be positive") Long employeeId) {
        return resourceManagerEmployeeService.listEmployeeAbsences(employeeId);
    }

    @GetMapping("/allocation-requests/pending")
    public List<ResourceManagerAllocationRequestListItemResponse> listPendingAllocationRequests() {
        return resourceManagerRequestService.listPendingRequests();
    }

    @GetMapping("/allocation-requests/{requestId}")
    public ResourceManagerAllocationRequestDetailsResponse getAllocationRequestDetails(
            @PathVariable @Positive(message = "Request id must be positive") Long requestId) {
        return resourceManagerRequestService.getRequestDetails(requestId);
    }

    @PostMapping("/allocation-requests/{requestId}/approve")
    public ResourceManagerAllocationRequestDetailsResponse approveAllocationRequest(
            @PathVariable @Positive(message = "Request id must be positive") Long requestId,
            @Valid @RequestBody(required = false) ReviewAllocationRequestDecision decision) {
        return resourceManagerRequestService.approveRequest(requestId, decision);
    }

    @PostMapping("/allocation-requests/{requestId}/reject")
    public ResourceManagerAllocationRequestDetailsResponse rejectAllocationRequest(
            @PathVariable @Positive(message = "Request id must be positive") Long requestId,
            @Valid @RequestBody(required = false) ReviewAllocationRequestDecision decision) {
        return resourceManagerRequestService.rejectRequest(requestId, decision);
    }
}

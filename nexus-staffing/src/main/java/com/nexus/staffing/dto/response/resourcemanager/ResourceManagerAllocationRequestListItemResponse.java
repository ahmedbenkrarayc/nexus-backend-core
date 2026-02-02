package com.nexus.staffing.dto.response.resourcemanager;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ResourceManagerAllocationRequestListItemResponse(
        Long requestId,
        Long projectId,
        String projectName,
        Long projectManagerEmployeeId,
        String projectManagerFirstName,
        String projectManagerLastName,
        Long employeeId,
        String employeeFirstName,
        String employeeLastName,
        String requiredRole,
        String engagementLevel,
        LocalDate requestedStartDate,
        LocalDate requestedEndDate,
        LocalDateTime createdAt,
        String status) {
}

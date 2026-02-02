package com.nexus.staffing.dto.response.resourcemanager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ResourceManagerAllocationRequestDetailsResponse(
        Long requestId,
        Long projectId,
        String projectName,
        String projectDescription,
        String projectCampusName,
        Long projectManagerEmployeeId,
        String projectManagerFirstName,
        String projectManagerLastName,
        Long employeeId,
        String employeeCode,
        String employeeFirstName,
        String employeeLastName,
        String employeeEmail,
        Long employeeCampusId,
        String requiredRole,
        String requiredSkill,
        String engagementLevel,
        LocalDate requestedStartDate,
        LocalDate requestedEndDate,
        String comment,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ResourceManagerEmployeeEngagementResponse> overlappingEngagements,
        List<ResourceManagerEmployeeAbsenceResponse> overlappingAbsences,
        ResourceManagerRequestConflictResponse conflict) {
}

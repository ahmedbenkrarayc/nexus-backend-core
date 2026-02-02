package com.nexus.staffing.dto.response.allocationrequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AllocationRequestResponse(
        Long id,
        Long projectId,
        Long createdByEmployeeId,
        String requiredRole,
        String requiredSkill,
        String engagementLevel,
        LocalDate startDate,
        LocalDate endDate,
        String comment,
        Long specificEmployeeId,
        AllocationRequestTrackingStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

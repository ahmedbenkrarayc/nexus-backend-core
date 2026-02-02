package com.nexus.staffing.dto.response.resourcemanager;

import com.nexus.staffing.model.enums.EngagementStatus;

import java.time.LocalDate;

public record ResourceManagerEmployeeEngagementResponse(
        Long engagementId,
        Long allocationRequestId,
        Long projectId,
        String projectName,
        String roleOnProject,
        String engagementLevel,
        LocalDate startDate,
        LocalDate endDate,
        EngagementStatus status
) {
}

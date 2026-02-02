package com.nexus.staffing.dto.request.allocationrequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateAllocationRequest(
        @NotNull(message = "Project id is required")
        Long projectId,

        @NotBlank(message = "Required role is required")
        String requiredRole,

        @NotBlank(message = "Required skill is required")
        String requiredSkill,

        @NotBlank(message = "Engagement level is required")
        String engagementLevel,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        LocalDate endDate,

        String comment,

        Long specificEmployeeId
) {
}

package com.nexus.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateProjectRequest(
        @NotBlank(message = "Project name is required")
        String name,

        String description,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        LocalDate endDate,

        String businessContext,

        String client
) {
}

package com.nexus.project.dto.request;

import com.nexus.project.model.enums.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateProjectRequest(
        @NotBlank(message = "Project name is required")
        String name,

        String description,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        LocalDate endDate,

        @NotNull(message = "Status is required")
        ProjectStatus status
) {
}

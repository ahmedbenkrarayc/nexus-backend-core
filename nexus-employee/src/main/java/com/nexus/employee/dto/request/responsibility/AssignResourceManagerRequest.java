package com.nexus.employee.dto.request.responsibility;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssignResourceManagerRequest(
        @NotNull(message = "Resource manager id is required")
        @Positive(message = "Resource manager id must be positive")
        Long resourceManagerId
) {
}
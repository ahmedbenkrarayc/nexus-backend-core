package com.nexus.employee.dto.request.responsibility;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ChangeResourceManagerRequest(
        @NotNull(message = "New resource manager id is required")
        @Positive(message = "New resource manager id must be positive")
        Long newResourceManagerId
) {
}
package com.nexus.staffing.dto.request.resourcemanager;

import jakarta.validation.constraints.Size;

public record ReviewAllocationRequestDecision(
        @Size(max = 1000, message = "Comment must not exceed 1000 characters")
        String comment
) {
}

package com.nexus.organization.dto.request.campus;

import com.nexus.organization.dto.request.location.LocationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCampusRequest(
        @NotBlank(message = "Campus name is required")
        @Size(max = 255, message = "Campus name must not exceed 255 characters")
        String name,

        @NotBlank(message = "Campus timezone is required")
        @Size(max = 100, message = "Campus timezone must not exceed 100 characters")
        String timezone,

        @Valid
        @NotNull(message = "Campus location is required")
        LocationRequest location
) {
}
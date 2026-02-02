package com.nexus.organization.dto.request.location;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LocationRequest(
        @NotBlank(message = "Country is required")
        @Size(max = 150, message = "Country must not exceed 150 characters")
        String country,

        @NotBlank(message = "City is required")
        @Size(max = 150, message = "City must not exceed 150 characters")
        String city,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be greater than or equal to -180")
        @DecimalMax(value = "180.0", message = "Longitude must be less than or equal to 180")
        Double longitude,

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be greater than or equal to -90")
        @DecimalMax(value = "90.0", message = "Latitude must be less than or equal to 90")
        Double latitude
) {
}
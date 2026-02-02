package com.nexus.organization.dto.response.location;

public record LocationResponse(
        Long id,
        String country,
        String city,
        double longitude,
        double latitude
) {
}
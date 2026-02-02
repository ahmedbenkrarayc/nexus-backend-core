package com.nexus.organization.dto.response.campus;

import com.nexus.organization.dto.response.location.LocationResponse;

public record CampusResponse(
        Long id,
        String name,
        boolean active,
        String timezone,
        Long organizationId,
        String organizationName,
        LocationResponse location
) {
}
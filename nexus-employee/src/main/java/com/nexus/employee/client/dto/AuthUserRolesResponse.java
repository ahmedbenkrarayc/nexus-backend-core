package com.nexus.employee.client.dto;

import java.util.Set;

public record AuthUserRolesResponse(
        Long userId,
        Set<String> roles
) {
}
package com.nexus.employee.client.dto;

import java.util.List;

public record AuthCurrentUserResponse(
        Long userId,
        List<String> roles
) {
}

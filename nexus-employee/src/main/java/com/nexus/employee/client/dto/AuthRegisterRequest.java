package com.nexus.employee.client.dto;

import java.util.Set;

public record AuthRegisterRequest(
        String username,
        String email,
        String password,
        Set<String> roles
) {
}
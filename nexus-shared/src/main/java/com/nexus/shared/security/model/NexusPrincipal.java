package com.nexus.shared.security.model;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;

/**
 * Authenticated user principal populated from the JWT access_token cookie.
 * Available as the principal in SecurityContextHolder after authentication.
 */
public record NexusPrincipal(
        Long userId,
        String username,
        List<GrantedAuthority> authorities
) {}

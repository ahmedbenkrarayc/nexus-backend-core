package com.nexus.shared.security.provider;

import com.nexus.shared.security.context.CurrentUserContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CurrentUserProvider {

    public CurrentUserContext getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || authentication instanceof AnonymousAuthenticationToken
                || authentication.getPrincipal() == null
                || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        Long userId = extractUserId(authentication.getPrincipal());
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unable to resolve authenticated user");
        }

        Set<String> roles = authentication.getAuthorities() == null
                ? Set.of()
                : authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(role -> role != null && !role.isBlank())
                .map(this::normalizeRoleName)
                .collect(Collectors.toSet());

        return new CurrentUserContext(userId, roles);
    }

    private Long extractUserId(Object principal) {
        try {
            Object value = principal.getClass().getMethod("userId").invoke(principal);
            if (value instanceof Number number) {
                return number.longValue();
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    private String normalizeRoleName(String role) {
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("ROLE_")) {
            return normalized.substring(5);
        }
        if (normalized.startsWith("SCOPE_")) {
            return normalized.substring(6);
        }
        if (normalized.startsWith("AUTHORITY_")) {
            return normalized.substring(10);
        }
        return normalized;
    }
}

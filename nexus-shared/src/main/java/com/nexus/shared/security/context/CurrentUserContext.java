package com.nexus.shared.security.context;

import java.util.Locale;
import java.util.Set;

public record CurrentUserContext(Long userId, Set<String> roles) {

	public boolean hasRole(String role) {
		return hasAnyRole(role);
	}

	public boolean hasAnyRole(String... expectedRoles) {
		if (roles == null || roles.isEmpty() || expectedRoles == null || expectedRoles.length == 0) {
			return false;
		}

		for (String expectedRole : expectedRoles) {
			if (expectedRole == null || expectedRole.isBlank()) {
				continue;
			}

			String normalizedExpectedRole = normalizeRoleName(expectedRole);
			if (roles.contains(normalizedExpectedRole)) {
				return true;
			}

			boolean suffixMatch = roles.stream()
					.anyMatch(actual -> actual.endsWith("_" + normalizedExpectedRole));
			if (suffixMatch) {
				return true;
			}
		}

		return false;
	}

	private static String normalizeRoleName(String role) {
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

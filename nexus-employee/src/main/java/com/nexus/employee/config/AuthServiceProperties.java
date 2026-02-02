package com.nexus.employee.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "employee.auth-service")
public record AuthServiceProperties(String baseUrl) {
}
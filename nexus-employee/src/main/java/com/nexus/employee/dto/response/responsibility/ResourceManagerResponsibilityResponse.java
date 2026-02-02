package com.nexus.employee.dto.response.responsibility;

import java.time.LocalDateTime;

public record ResourceManagerResponsibilityResponse(
        Long responsibilityId,
        Long employeeId,
        String employeeCode,
        String employeeFirstName,
        String employeeLastName,
        Long resourceManagerId,
        String resourceManagerCode,
        String resourceManagerFirstName,
        String resourceManagerLastName,
        String type,
        LocalDateTime startDate,
        LocalDateTime endDate,
        boolean active
) {
}
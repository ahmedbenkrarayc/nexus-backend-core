package com.nexus.employee.dto.response.responsibility;

import java.time.LocalDateTime;

public record EmployeeUnderResourceManagerResponse(
        Long responsibilityId,
        Long employeeId,
        String employeeCode,
        String employeeFirstName,
        String employeeLastName,
        String employeeEmail,
        Long campusId,
        LocalDateTime assignedAt
) {
}
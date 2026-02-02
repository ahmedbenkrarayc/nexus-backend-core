package com.nexus.employee.dto.response.employee;

import java.time.LocalDateTime;

public record EmployeeResponse(
        Long id,
        String fname,
        String lname,
        String code,
        String email,
        Long authUser,
        Long campusId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
package com.nexus.employee.dto.response.employee;

public record EmployeeListItemResponse(
        Long id,
        String code,
        String fname,
        String lname,
        String email,
        Long campusId
) {
}
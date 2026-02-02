package com.nexus.staffing.dto.response.resourcemanager;

public record ResourceManagerEmployeeListItemResponse(
        Long employeeId,
        String employeeCode,
        String firstName,
        String lastName,
        String email,
        Long campusId) {
}

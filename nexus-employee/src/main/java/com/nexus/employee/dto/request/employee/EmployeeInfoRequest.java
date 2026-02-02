package com.nexus.employee.dto.request.employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmployeeInfoRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 120, message = "First name must not exceed 120 characters")
        String fname,

        @NotBlank(message = "Last name is required")
        @Size(max = 120, message = "Last name must not exceed 120 characters")
        String lname,

        @NotBlank(message = "Employee code is required")
        @Size(max = 100, message = "Employee code must not exceed 100 characters")
        String code,

        @NotNull(message = "Campus id is required")
        Long campusId
) {
}
package com.nexus.employee.dto.request.employee;

import com.nexus.employee.dto.request.user.UserCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CreateEmployeeRequest(
        @Valid
        @NotNull(message = "Employee info is required")
        EmployeeInfoRequest employee,

        @Valid
        @NotNull(message = "User info is required")
        UserCreateRequest user
) {
}
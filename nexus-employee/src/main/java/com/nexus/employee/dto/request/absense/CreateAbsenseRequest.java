package com.nexus.employee.dto.request.absense;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateAbsenseRequest(
    @NotNull(message = "Employee ID is required")
    Long employeeId,

    @NotBlank(message = "Absence type is required")
    String type,

    @NotNull(message = "Start date is required")
    LocalDate start,

    @NotNull(message = "End date is required")
    LocalDate end,

    String comment
) {
}

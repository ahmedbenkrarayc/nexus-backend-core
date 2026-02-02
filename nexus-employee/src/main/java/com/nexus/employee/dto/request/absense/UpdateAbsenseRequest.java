package com.nexus.employee.dto.request.absense;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UpdateAbsenseRequest(
    @NotBlank(message = "Absence type is required")
    String type,

    @NotNull(message = "Start date is required")
    LocalDate start,

    @NotNull(message = "End date is required")
    LocalDate end,

    @NotNull(message = "Approved status is required")
    Boolean approved,

    String comment
) {
}

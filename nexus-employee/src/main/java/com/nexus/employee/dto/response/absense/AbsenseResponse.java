package com.nexus.employee.dto.response.absense;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AbsenseResponse(
    Long id,
    Long employeeId,
    String employeeName,
    String type,
    LocalDate start,
    LocalDate end,
    boolean approved,
    String comment,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

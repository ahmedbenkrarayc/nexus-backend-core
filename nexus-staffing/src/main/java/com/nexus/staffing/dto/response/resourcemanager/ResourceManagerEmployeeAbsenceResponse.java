package com.nexus.staffing.dto.response.resourcemanager;

import java.time.LocalDate;

public record ResourceManagerEmployeeAbsenceResponse(
        Long absenceId,
        String type,
        LocalDate start,
        LocalDate end,
        boolean approved,
        String comment
) {
}

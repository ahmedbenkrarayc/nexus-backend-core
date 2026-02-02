package com.nexus.staffing.dto.response.engagement;

import java.time.LocalDate;

public record ProjectTeamMemberResponse(
        Long employeeId,
        String firstName,
        String lastName,
        Long campusId,
        String roleOnProject,
        String engagementLevel,
        LocalDate startDate,
        LocalDate endDate
) {
}

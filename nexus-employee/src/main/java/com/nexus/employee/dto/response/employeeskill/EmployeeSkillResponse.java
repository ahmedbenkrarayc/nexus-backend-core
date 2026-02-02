package com.nexus.employee.dto.response.employeeskill;

import java.time.LocalDateTime;

public record EmployeeSkillResponse(
        Long id,
        Long skillId,
        String skillName,
        String skillCategory,
        String level,
        LocalDateTime lastUpdatedAt
) {
}

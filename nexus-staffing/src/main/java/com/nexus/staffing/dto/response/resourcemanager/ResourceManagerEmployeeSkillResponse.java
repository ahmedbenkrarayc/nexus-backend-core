package com.nexus.staffing.dto.response.resourcemanager;

import java.time.LocalDateTime;

public record ResourceManagerEmployeeSkillResponse(
        Long skillId,
        String skillName,
        String skillCategory,
        String level,
        LocalDateTime lastUpdatedAt
) {
}

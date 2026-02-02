package com.nexus.employee.dto.response.skill;

import java.time.LocalDateTime;

public record SkillResponse(
        Long id,
        String name,
        String category,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
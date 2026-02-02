package com.nexus.employee.dto.request.skill;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSkillRequest(
        @NotBlank(message = "Skill name is required")
        @Size(max = 150, message = "Skill name must not exceed 150 characters")
        String name,

        @NotBlank(message = "Skill category is required")
        @Size(max = 150, message = "Skill category must not exceed 150 characters")
        String category
) {
}
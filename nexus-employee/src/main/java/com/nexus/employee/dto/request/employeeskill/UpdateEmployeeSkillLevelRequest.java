package com.nexus.employee.dto.request.employeeskill;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateEmployeeSkillLevelRequest(

        @NotBlank(message = "Skill level is required")
        @Size(max = 100, message = "Skill level must not exceed 100 characters")
        String level
) {
}

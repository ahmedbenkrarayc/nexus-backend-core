package com.nexus.employee.dto.request.employeeskill;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AddEmployeeSkillsRequest(

        @NotEmpty(message = "At least one skill is required")
        @Valid
        List<AddEmployeeSkillRequest> skills
) {
}

package com.nexus.staffing.dto.request.candidatesearch;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record SearchCandidatesRequest(
        @NotNull(message = "Project id is required")
        Long projectId,

        @NotEmpty(message = "At least one required skill is required")
        List<String> requiredSkills,

        List<String> niceToHaveSkills,

        Map<String, String> minimumSkillLevels,

        @NotNull(message = "Engagement level is required")
        String engagementLevel,

        @NotNull(message = "Requested start date is required")
        LocalDate requestedStartDate,

        @NotNull(message = "Requested end date is required")
        LocalDate requestedEndDate,

        Integer maxResults
) {
        public int getMaxResults() {
                return maxResults != null ? maxResults : 20;
        }
}

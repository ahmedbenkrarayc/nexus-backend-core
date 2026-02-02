package com.nexus.staffing.dto.response.candidatesearch;

import com.nexus.staffing.model.enums.LocationPriorityCategory;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record CandidateMatchResponse(
        Long employeeId,
        String firstName,
        String lastName,
        String campusName,
        String country,
        Boolean available,
        Boolean availableForRequestedPeriod,
        Integer matchPercentage,
        LocationPriorityCategory locationPriorityCategory,
        List<String> matchedRequiredSkills,
        List<String> missingRequiredSkills,
        List<String> matchedNiceToHaveSkills,
        Integer overlappingEngagementCount,
        Boolean hasAbsenceConflict,
        List<String> explanation,
        Map<String, Integer> scoreBreakdown
) {
}

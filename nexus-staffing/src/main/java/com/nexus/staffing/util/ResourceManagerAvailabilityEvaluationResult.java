package com.nexus.staffing.util;

import java.time.LocalDate;
import java.util.List;

public record ResourceManagerAvailabilityEvaluationResult(
        boolean hasAbsenceConflict,
        int overlappingEngagementCount,
        boolean hasEngagementLevelConflict,
        int overlappingEngagementLoad,
        int requestedEngagementLoad,
        boolean availableForRequestedPeriod,
        List<String> explanations,
        LocalDate requestedStart,
        LocalDate requestedEnd) {
}

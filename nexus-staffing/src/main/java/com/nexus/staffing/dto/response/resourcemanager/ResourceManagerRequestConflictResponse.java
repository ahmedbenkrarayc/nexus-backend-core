package com.nexus.staffing.dto.response.resourcemanager;

import java.util.List;

public record ResourceManagerRequestConflictResponse(
        boolean hasAbsenceConflict,
        int overlappingEngagementCount,
        boolean hasEngagementLevelConflict,
        int overlappingEngagementLoad,
        int requestedEngagementLoad,
        boolean availableForRequestedPeriod,
        List<String> explanations
) {
}

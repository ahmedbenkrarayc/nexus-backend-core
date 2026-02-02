package com.nexus.staffing.allocationrequest;

import java.time.LocalDate;

/** Parsed structured fields embedded in allocation request comments after the PM metadata marker. */
public record AllocationRequestParsedMetadata(
        String requiredRole,
        String requiredSkill,
        String engagementLevel,
        LocalDate startDate,
        LocalDate endDate,
        Long specificEmployeeId,
        String userComment) {

    public static AllocationRequestParsedMetadata empty() {
        return new AllocationRequestParsedMetadata(null, null, null, null, null, null, null);
    }

    public static AllocationRequestParsedMetadata withCommentOnly(String comment) {
        return new AllocationRequestParsedMetadata(null, null, null, null, null, null, comment);
    }

    public boolean hasStructuredData() {
        return requiredRole != null
                || requiredSkill != null
                || engagementLevel != null
                || startDate != null
                || endDate != null
                || specificEmployeeId != null;
    }
}

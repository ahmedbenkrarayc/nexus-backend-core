package com.nexus.staffing.allocationrequest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses PM-embedded metadata from allocation request {@code comment} text.
 */
public final class AllocationRequestMetadataParser {

    public static final String METADATA_MARKER = "[[PM_REQUEST_METADATA]]";

    private AllocationRequestMetadataParser() {
    }

    public static AllocationRequestParsedMetadata parse(String persistedComment) {
        if (persistedComment == null || persistedComment.isBlank()) {
            return AllocationRequestParsedMetadata.empty();
        }

        int markerIndex = persistedComment.indexOf(METADATA_MARKER);
        if (markerIndex < 0) {
            return AllocationRequestParsedMetadata.withCommentOnly(persistedComment.trim());
        }

        String userComment = persistedComment.substring(0, markerIndex).trim();
        String metadataBlock = persistedComment.substring(markerIndex + METADATA_MARKER.length()).trim();

        Map<String, String> metadata = new HashMap<>();
        for (String line : metadataBlock.split("\\R")) {
            int equalsIndex = line.indexOf('=');
            if (equalsIndex <= 0) {
                continue;
            }
            String key = line.substring(0, equalsIndex).trim();
            String value = line.substring(equalsIndex + 1).trim();
            if (!key.isEmpty() && !value.isEmpty()) {
                metadata.put(key, value);
            }
        }

        return new AllocationRequestParsedMetadata(
                metadata.get("requiredRole"),
                metadata.get("requiredSkill"),
                metadata.get("engagementLevel"),
                parseLocalDate(metadata.get("startDate")),
                parseLocalDate(metadata.get("endDate")),
                parseLong(metadata.get("specificEmployeeId")),
                userComment.isEmpty() ? null : userComment);
    }

    private static LocalDate parseLocalDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (RuntimeException ignored) {
            return null;
        }
    }
}

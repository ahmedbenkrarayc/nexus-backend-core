package com.nexus.staffing.util;

import com.nexus.employee.model.Absense;
import com.nexus.staffing.model.Engagement;
import com.nexus.staffing.model.enums.EngagementStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ResourceManagerAvailabilityEvaluator {

    private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("(\\\\d{1,3})");

    public ResourceManagerAvailabilityEvaluationResult evaluate(
            List<Absense> overlappingAbsences,
            List<Engagement> overlappingEngagements,
            String requestedEngagementLevel,
            LocalDate requestedStart,
            LocalDate requestedEnd) {

        boolean hasAbsenceConflict = overlappingAbsences.stream().anyMatch(Absense::isApproved);
        int overlappingEngagementCount = (int) overlappingEngagements.stream()
                .filter(engagement -> isActiveStatus(engagement.getStatus()))
                .count();

        int overlappingLoad = overlappingEngagements.stream()
                .filter(engagement -> isActiveStatus(engagement.getStatus()))
                .map(Engagement::getEngagementLevel)
                .mapToInt(this::toLoadPercentage)
                .sum();

        int requestedLoad = toLoadPercentage(requestedEngagementLevel);
        boolean hasEngagementLevelConflict = (overlappingLoad + requestedLoad) > 100;

        boolean availableForRequestedPeriod = !hasAbsenceConflict
                && !hasEngagementLevelConflict
                && overlappingEngagementCount <= 1;

        List<String> explanations = new ArrayList<>();
        if (hasAbsenceConflict) {
            explanations.add("Has approved absence conflict in requested period");
        } else {
            explanations.add("No approved absence conflict in requested period");
        }

        if (overlappingEngagementCount == 0) {
            explanations.add("No overlapping active engagements in requested period");
        } else if (overlappingEngagementCount == 1) {
            explanations.add("1 overlapping active engagement detected");
        } else {
            explanations.add(overlappingEngagementCount + " overlapping active engagements detected");
        }

        if (hasEngagementLevelConflict) {
            explanations.add("Engagement level conflict: requested load " + requestedLoad
                    + "% with overlapping load " + overlappingLoad + "% exceeds 100% capacity");
        } else {
            explanations.add("Engagement load is within capacity: requested load " + requestedLoad
                    + "% and overlapping load " + overlappingLoad + "%");
        }

        return new ResourceManagerAvailabilityEvaluationResult(
                hasAbsenceConflict,
                overlappingEngagementCount,
                hasEngagementLevelConflict,
                overlappingLoad,
                requestedLoad,
                availableForRequestedPeriod,
                explanations,
                requestedStart,
                requestedEnd);
    }

    private boolean isActiveStatus(EngagementStatus status) {
        return status == EngagementStatus.PLANNED || status == EngagementStatus.ACTIVE;
    }

    private int toLoadPercentage(String engagementLevel) {
        if (engagementLevel == null || engagementLevel.isBlank()) {
            return 50;
        }

        String normalized = engagementLevel.trim().toUpperCase(Locale.ROOT);

        Matcher matcher = PERCENTAGE_PATTERN.matcher(normalized);
        if (matcher.find()) {
            int parsed = Integer.parseInt(matcher.group(1));
            return Math.max(0, Math.min(100, parsed));
        }

        return switch (normalized) {
            case "CRITICAL", "FULL_TIME", "FULL", "MAX" -> 100;
            case "HIGH" -> 80;
            case "MEDIUM" -> 60;
            case "LOW" -> 40;
            case "HALF" -> 50;
            case "MINIMAL", "MIN" -> 20;
            default -> 50;
        };
    }
}

package com.nexus.staffing.util;

import com.nexus.employee.model.Absense;
import com.nexus.employee.model.Employee;
import com.nexus.staffing.model.Engagement;
import com.nexus.staffing.model.enums.EngagementStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Evaluates employee availability for a requested period.
 * Detects conflicts with absences and existing engagements.
 */
@Component
@RequiredArgsConstructor
public class EmployeeAvailabilityEvaluator {

    /**
     * Checks if employee has absence conflicts in the requested period.
     * 
     * @param absences List of employee absences
     * @param requestedStart Period start date (inclusive)
     * @param requestedEnd Period end date (inclusive)
     * @return true if approved absence overlaps requested period
     */
    public boolean hasAbsenceConflict(List<Absense> absences, LocalDate requestedStart, LocalDate requestedEnd) {
        return absences.stream()
            .filter(Absense::isApproved)
            .anyMatch(absence -> DateOverlapHelper.hasOverlap(
                absence.getStart(), 
                absence.getEnd(),
                requestedStart, 
                requestedEnd
            ));
    }

    /**
     * Counts overlapping engagements during the requested period.
     * Only counts PLANNED or ACTIVE engagements (not COMPLETED/CANCELLED).
     * 
     * @param engagements List of employee's engagements
     * @param requestedStart Period start date (inclusive)
     * @param requestedEnd Period end date (inclusive)
     * @return Count of overlapping engagements
     */
    public int countOverlappingEngagements(List<Engagement> engagements, LocalDate requestedStart, LocalDate requestedEnd) {
        return (int) engagements.stream()
            .filter(e -> isActiveEngagement(e.getStatus()))
            .filter(e -> DateOverlapHelper.hasOverlap(
                e.getStartDate(), 
                e.getEndDate(),
                requestedStart, 
                requestedEnd
            ))
            .count();
    }

    /**
     * Evaluates overall availability.
     * @return true if no absence conflict AND overlapping engagements <= 1 (allowing minor overlap)
     */
    public boolean isAvailable(List<Absense> absences, List<Engagement> engagements,
                               LocalDate requestedStart, LocalDate requestedEnd) {
        return !hasAbsenceConflict(absences, requestedStart, requestedEnd)
            && countOverlappingEngagements(engagements, requestedStart, requestedEnd) <= 1;
    }

    /**
     * Gets list of explanation strings for availability issues.
     */
    public List<String> getAvailabilityExplanations(List<Absense> absences, List<Engagement> engagements,
                                                    LocalDate requestedStart, LocalDate requestedEnd) {
        List<String> explanations = new java.util.ArrayList<>();

        if (hasAbsenceConflict(absences, requestedStart, requestedEnd)) {
            explanations.add("Has approved absence conflict in requested period");
        }

        int overlappingCount = countOverlappingEngagements(engagements, requestedStart, requestedEnd);
        if (overlappingCount == 0) {
            explanations.add("No overlapping engagements in requested period");
        } else if (overlappingCount == 1) {
            explanations.add("1 overlapping engagement detected during requested period");
        } else {
            explanations.add(overlappingCount + " overlapping engagements detected during requested period");
        }

        return explanations;
    }

    /**
     * Check if engagement status indicates it's active/ongoing.
     */
    private boolean isActiveEngagement(EngagementStatus status) {
        return status == EngagementStatus.PLANNED || status == EngagementStatus.ACTIVE;
    }
}

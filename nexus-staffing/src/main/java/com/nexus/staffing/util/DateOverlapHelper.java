package com.nexus.staffing.util;

import java.time.LocalDate;

/**
 * Utility class for date overlap detection.
 * Determines if two date ranges overlap.
 */
public class DateOverlapHelper {

    private DateOverlapHelper() {
        // Utility class
    }

    /**
     * Checks if two date ranges overlap.
     * Inclusive on both ends.
     * 
     * @param start1 First range start (inclusive)
     * @param end1 First range end (inclusive)
     * @param start2 Second range start (inclusive)
     * @param end2 Second range end (inclusive)
     * @return true if ranges overlap, false otherwise
     */
    public static boolean hasOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        if (start1 == null || start2 == null) {
            return false;
        }

        // Handle null end dates (open-ended ranges)
        if (end1 == null) {
            end1 = LocalDate.parse("2099-12-31");
        }
        if (end2 == null) {
            end2 = LocalDate.parse("2099-12-31");
        }

        // Two ranges overlap if: start1 <= end2 AND start2 <= end1
        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }

    /**
     * Checks if a date range falls completely within another range.
     * 
     * @param testStart Start of range to test (inclusive)
     * @param testEnd End of range to test (inclusive)
     * @param containerStart Start of container range (inclusive)
     * @param containerEnd End of container range (inclusive)
     * @return true if test range is contained within container, false otherwise
     */
    public static boolean isContainedWithin(LocalDate testStart, LocalDate testEnd,
                                             LocalDate containerStart, LocalDate containerEnd) {
        if (testStart == null || containerStart == null) {
            return false;
        }

        LocalDate actualTestEnd = testEnd != null ? testEnd : LocalDate.parse("2099-12-31");
        LocalDate actualContainerEnd = containerEnd != null ? containerEnd : LocalDate.parse("2099-12-31");

        return !testStart.isBefore(containerStart) && !actualTestEnd.isAfter(actualContainerEnd);
    }
}

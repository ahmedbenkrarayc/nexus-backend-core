package com.nexus.staffing.util;

import com.nexus.staffing.model.enums.LocationPriorityCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scoring engine for candidate matching.
 * Allocates points across multiple dimensions:
 * - 40 pts: Required skills match
 * - 15 pts: Nice-to-have skills match
 * - 15 pts: Skill level match
 * - 15 pts: Location priority
 * - 15 pts: Availability/load
 */
@Component
@RequiredArgsConstructor
public class EmployeeScoringEngine {

    private static final int REQUIRED_SKILLS_MAX = 40;
    private static final int NICE_SKILLS_MAX = 15;
    private static final int LEVEL_MATCH_MAX = 15;
    private static final int LOCATION_MAX = 15;
    private static final int AVAILABILITY_MAX = 15;
    private static final int TOTAL_POINTS = 100;

    /**
     * Calculates match percentage (0-100).
     * 
     * @param matchedRequiredCount Number of matched required skills
     * @param totalRequiredCount Total required skills
     * @param matchedNiceCount Number of matched nice-to-have skills
     * @param totalNiceCount Total nice-to-have skills
     * @param skillLevelPenalty Penalty for not meeting minimum levels (0-15)
     * @param locationScore Location priority score (0-15)
     * @param availabilityScore Availability/load score (0-15)
     * @return Match percentage (0-100)
     */
    public EmployeeScoringResult calculateScore(
            int matchedRequiredCount,
            int totalRequiredCount,
            int matchedNiceCount,
            int totalNiceCount,
            int skillLevelPenalty,
            int locationScore,
            int availabilityScore,
            boolean hasAbsenceConflict,
            int overlappingEngagementCount
    ) {
        Map<String, Integer> breakdown = new HashMap<>();
        int totalScore = 0;

        // 1. Required skills (40 points max)
        int requiredScore = calculateRequiredSkillsScore(matchedRequiredCount, totalRequiredCount);
        breakdown.put("requiredSkills", requiredScore);
        totalScore += requiredScore;

        // 2. Nice-to-have skills (15 points max)  
        int niceScore = calculateNiceToHaveScore(matchedNiceCount, totalNiceCount);
        breakdown.put("niceToHaveSkills", niceScore);
        totalScore += niceScore;

        // 3. Skill level match (15 points max, reduced by penalty)
        int levelScore = Math.max(0, LEVEL_MATCH_MAX - skillLevelPenalty);
        breakdown.put("skillLevelMatch", levelScore);
        totalScore += levelScore;

        // 4. Location priority (15 points)
        breakdown.put("location", locationScore);
        totalScore += locationScore;

        // 5. Availability/load (15 points max, reduced by conflicts)
        int availScore = calculateAvailabilityScore(
            hasAbsenceConflict,
            overlappingEngagementCount,
            availabilityScore
        );
        breakdown.put("availability", availScore);
        totalScore += availScore;

        int percentage = Math.min(100, (totalScore * 100) / TOTAL_POINTS);

        return new EmployeeScoringResult(percentage, breakdown);
    }

    /**
     * Scores required skills matching.
     * If all required skills matched: 40 pts
     * Proportional scoring if partial match
     */
    private int calculateRequiredSkillsScore(int matchedCount, int totalCount) {
        if (totalCount == 0) {
            return REQUIRED_SKILLS_MAX;
        }
        double ratio = (double) matchedCount / totalCount;
        if (ratio >= 1.0) {
            return REQUIRED_SKILLS_MAX;
        }
        if (ratio >= 0.75) {
            return 35;
        }
        if (ratio >= 0.5) {
            return 25;
        }
        if (ratio >= 0.25) {
            return 15;
        }
        return 5;
    }

    /**
     * Scores nice-to-have skills.
     * Only adds points if at least one is matched.
     */
    private int calculateNiceToHaveScore(int matchedCount, int totalCount) {
        if (totalCount == 0) {
            return NICE_SKILLS_MAX;
        }
        if (matchedCount == 0) {
            return 0;
        }
        double ratio = (double) matchedCount / totalCount;
        if (ratio >= 0.75) {
            return NICE_SKILLS_MAX;
        }
        if (ratio >= 0.5) {
            return 10;
        }
        return 5;
    }

    /**
     * Scores availability and current load.
     */
    private int calculateAvailabilityScore(boolean hasAbsence, int overlappingCount, int baseScore) {
        int score = baseScore;

        // Absence conflict is a major issue
        if (hasAbsence) {
            score = Math.max(0, score - 10);
        }

        // Heavy overlapping engagements reduce score
        if (overlappingCount > 2) {
            score = Math.max(0, score - 8);
        } else if (overlappingCount == 2) {
            score = Math.max(0, score - 5);
        }

        return Math.min(AVAILABILITY_MAX, score);
    }
}

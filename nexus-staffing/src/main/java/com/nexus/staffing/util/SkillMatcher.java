package com.nexus.staffing.util;

import com.nexus.employee.model.EmployeeSkill;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Matches employee skills against required and nice-to-have skills.
 */
@Component
@RequiredArgsConstructor
public class SkillMatcher {

    /**
     * Matches employee skills against required skills.
     * 
     * @param employeeSkills Employee's skills
     * @param requiredSkills Required skill names
     * @return List of matched skill names
     */
    public List<String> getMatchedRequiredSkills(List<EmployeeSkill> employeeSkills, List<String> requiredSkills) {
        Set<String> requiredSet = requiredSkills.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
            
        return employeeSkills.stream()
            .map(es -> es.getSkill().getName())
            .filter(name -> requiredSet.contains(name.toLowerCase()))
            .toList();
    }

    /**
     * Identifies missing required skills.
     * 
     * @param employeeSkills Employee's skills
     * @param requiredSkills Required skill names
     * @return List of required skills not found in employee's skills
     */
    public List<String> getMissingRequiredSkills(List<EmployeeSkill> employeeSkills, List<String> requiredSkills) {
        Set<String> employeeSkillNames = employeeSkills.stream()
            .map(es -> es.getSkill().getName().toLowerCase())
            .collect(Collectors.toSet());

        return requiredSkills.stream()
            .filter(skill -> !employeeSkillNames.contains(skill.toLowerCase()))
            .toList();
    }

    /**
     * Matches employee skills against nice-to-have skills.
     * 
     * @param employeeSkills Employee's skills
     * @param niceToHaveSkills Nice-to-have skill names
     * @return List of matched skill names from nice-to-have list
     */
    public List<String> getMatchedNiceToHaveSkills(List<EmployeeSkill> employeeSkills, List<String> niceToHaveSkills) {
        Set<String> niceSet = (niceToHaveSkills != null ? niceToHaveSkills : new ArrayList<String>()).stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
            
        return employeeSkills.stream()
            .map(es -> es.getSkill().getName())
            .filter(name -> niceSet.contains(name.toLowerCase()))
            .toList();
    }

    /**
     * Validates if employee skills meet minimum level requirements.
     * 
     * @param employeeSkills Employee's skills (with levels)
     * @param minimumLevels Map of skill name to minimum level required
     * @return List of skills that don't meet minimum level (important for explanation)
     */
    public List<String> getSkillsNotMeetingMinimumLevel(List<EmployeeSkill> employeeSkills, 
                                                        Map<String, String> minimumLevels) {
        if (minimumLevels == null || minimumLevels.isEmpty()) {
            return List.of();
        }

        List<String> failingSkills = new ArrayList<>();

        for (EmployeeSkill es : employeeSkills) {
            String skillName = es.getSkill().getName();
            String minimumLevel = minimumLevels.get(skillName);

            if (minimumLevel != null && !meetLevelRequirement(es.getLevel(), minimumLevel)) {
                failingSkills.add(skillName + " (have: " + es.getLevel() + ", need: " + minimumLevel + ")");
            }
        }

        return failingSkills;
    }

    /**
     * Simple level comparison. Assumes level strings are comparable (e.g., "Junior", "Mid", "Senior")
     * or numeric. This is a basic implementation.
     */
    private boolean meetLevelRequirement(String employeeLevel, String minimumLevel) {
        // Simple string comparison - can be enhanced for custom level hierarchies
        // For MVP, we use a simple numeric/alphabetic approach
        if (employeeLevel == null) {
            return false;
        }

        Map<String, Integer> levelRank = Map.of(
            "junior", 1,
            "mid", 2,
            "senior", 3,
            "lead", 4,
            "expert", 5
        );

        Integer employeeRank = levelRank.get(employeeLevel.toLowerCase());
        Integer minimumRank = levelRank.get(minimumLevel.toLowerCase());

        if (employeeRank == null || minimumRank == null) {
            // If not recognized, do simple string comparison (case-insensitive)
            return employeeLevel.compareToIgnoreCase(minimumLevel) >= 0;
        }

        return employeeRank >= minimumRank;
    }
}

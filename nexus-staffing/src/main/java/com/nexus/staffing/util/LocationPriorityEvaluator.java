package com.nexus.staffing.util;

import com.nexus.organization.model.Campus;
import com.nexus.organization.model.Location;
import com.nexus.staffing.model.enums.LocationPriorityCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Evaluates location priority between a project and an employee.
 * Priority: same campus > same country > different country
 */
@Component
@RequiredArgsConstructor
public class LocationPriorityEvaluator {

    /**
     * Determines location priority category.
     * 
     * @param projectCampus The project's campus
     * @param employeeCampus The employee's campus
     * @return LocationPriorityCategory
     */
    public LocationPriorityCategory evaluatePriority(Campus projectCampus, Campus employeeCampus) {
        if (projectCampus == null || employeeCampus == null) {
            return LocationPriorityCategory.DIFFERENT_COUNTRY;
        }

        // Same campus
        if (Objects.equals(projectCampus.getId(), employeeCampus.getId())) {
            return LocationPriorityCategory.SAME_CAMPUS;
        }

        // Same country but different campus
        String projectCountry = getCountry(projectCampus);
        String employeeCountry = getCountry(employeeCampus);

        if (projectCountry != null && employeeCountry != null 
            && projectCountry.equalsIgnoreCase(employeeCountry)) {
            return LocationPriorityCategory.SAME_COUNTRY;
        }

        return LocationPriorityCategory.DIFFERENT_COUNTRY;
    }

    /**
     * Returns the country for a campus (null-safe).
     */
    private String getCountry(Campus campus) {
        if (campus != null && campus.getLocation() != null) {
            return campus.getLocation().getCountry();
        }
        return null;
    }

    /**
     * Returns scored points for location priority (0-15 points).
     */
    public int scoreLocationPriority(LocationPriorityCategory category) {
        return switch (category) {
            case SAME_CAMPUS -> 15;
            case SAME_COUNTRY -> 10;
            case DIFFERENT_COUNTRY -> 3;
        };
    }
}

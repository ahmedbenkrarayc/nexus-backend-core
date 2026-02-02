package com.nexus.staffing.resourcemanager;

import com.nexus.employee.model.Employee;
import com.nexus.project.model.Project;
import com.nexus.staffing.model.AllocationRequest;
import com.nexus.staffing.model.Engagement;

import java.time.LocalDate;

/**
 * Resolved state for reviewing a single allocation request as resource manager.
 */
public record ResourceManagerAllocationRequestContext(
        Employee resourceManager,
        AllocationRequest request,
        Project project,
        Employee projectManager,
        Employee employee,
        Engagement primaryEngagement,
        String requiredRole,
        String requiredSkill,
        String engagementLevel,
        LocalDate requestedStartDate,
        LocalDate requestedEndDate,
        String comment,
        String status) {
}

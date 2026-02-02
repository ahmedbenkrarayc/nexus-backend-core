package com.nexus.staffing.dto.response.resourcemanager;

import java.util.List;

public record ResourceManagerEmployeeDetailsResponse(
                Long employeeId,
                String employeeCode,
                String firstName,
                String lastName,
                String email,
                Long campusId,
                List<ResourceManagerEmployeeSkillResponse> skills,
                List<ResourceManagerEmployeeAbsenceResponse> absences,
                List<ResourceManagerEmployeeEngagementResponse> engagements) {
}

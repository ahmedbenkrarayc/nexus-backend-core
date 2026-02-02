package com.nexus.staffing.dto.response.employee;

import com.nexus.project.model.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeProjectDetailsResponse {
    private Long id;
    private String name;
    private String description;
    private String ownerCampus;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectStatus status;
    private EmployeeEngagementResponse myEngagement;
}

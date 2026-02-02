package com.nexus.staffing.dto.response.employee;

import com.nexus.staffing.model.enums.EngagementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeEngagementResponse {
    private Long engagementId;
    private Long projectId;
    private String projectName;
    private String projectCampus;
    private String roleOnProject;
    private String engagementLevel;
    private LocalDate startDate;
    private LocalDate endDate;
    private EngagementStatus status;
}

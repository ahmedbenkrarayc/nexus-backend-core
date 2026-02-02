package com.nexus.staffing.dto.response.employee;

import com.nexus.staffing.dto.response.allocationrequest.AllocationRequestTrackingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAllocationRequestResponse {
    private Long requestId;
    private Long projectId;
    private String projectName;
    private String projectCampus;
    private String requestedRole;
    private String requestedEngagementLevel;
    private LocalDate requestedStartDate;
    private LocalDate requestedEndDate;
    private AllocationRequestTrackingStatus status;
    private String requesterName;
    private String comment;
}

package com.nexus.staffing.service;

import com.nexus.staffing.dto.request.allocationrequest.CreateAllocationRequest;
import com.nexus.staffing.dto.response.allocationrequest.AllocationRequestResponse;
import com.nexus.staffing.dto.response.engagement.ProjectTeamMemberResponse;

import java.util.List;

public interface ProjectManagerStaffingService {

    List<ProjectTeamMemberResponse> listProjectTeam(Long projectId);

    AllocationRequestResponse createAllocationRequest(CreateAllocationRequest request);

    List<AllocationRequestResponse> listAllocationRequests(Long projectId);
}

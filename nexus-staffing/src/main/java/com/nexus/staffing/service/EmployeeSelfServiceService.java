package com.nexus.staffing.service;

import com.nexus.employee.dto.response.absense.AbsenseResponse;
import com.nexus.employee.dto.response.employeeskill.EmployeeSkillResponse;
import com.nexus.staffing.dto.response.employee.EmployeeAllocationRequestResponse;
import com.nexus.staffing.dto.response.employee.EmployeeEngagementResponse;
import com.nexus.staffing.dto.response.employee.EmployeeProjectDetailsResponse;

import java.util.List;

public interface EmployeeSelfServiceService {

    List<EmployeeEngagementResponse> getMyEngagements();

    EmployeeProjectDetailsResponse getMyProjectDetails(Long projectId);

    List<EmployeeAllocationRequestResponse> getMyAllocationRequests();

    EmployeeAllocationRequestResponse getMyAllocationRequestDetails(Long requestId);

    List<EmployeeSkillResponse> getMySkills();

    List<AbsenseResponse> getMyAbsences();
}

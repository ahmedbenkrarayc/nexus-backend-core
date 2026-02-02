package com.nexus.staffing.service;

import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerEmployeeAbsenceResponse;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerEmployeeDetailsResponse;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerEmployeeEngagementResponse;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerEmployeeListItemResponse;

import java.util.List;

public interface ResourceManagerEmployeeService {

    List<ResourceManagerEmployeeListItemResponse> listManagedEmployees();

    ResourceManagerEmployeeDetailsResponse getManagedEmployeeDetails(Long employeeId);

    List<ResourceManagerEmployeeEngagementResponse> listCurrentEngagements(Long employeeId);

    List<ResourceManagerEmployeeAbsenceResponse> listEmployeeAbsences(Long employeeId);
}

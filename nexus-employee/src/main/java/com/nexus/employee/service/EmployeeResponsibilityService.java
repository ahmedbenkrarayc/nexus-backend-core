package com.nexus.employee.service;

import com.nexus.employee.dto.request.responsibility.AssignResourceManagerRequest;
import com.nexus.employee.dto.request.responsibility.ChangeResourceManagerRequest;
import com.nexus.employee.dto.response.responsibility.EmployeeUnderResourceManagerResponse;
import com.nexus.employee.dto.response.responsibility.ResourceManagerResponsibilityResponse;

import java.util.List;

public interface EmployeeResponsibilityService {

    ResourceManagerResponsibilityResponse assignResourceManager(Long employeeId, AssignResourceManagerRequest request);

    ResourceManagerResponsibilityResponse changeResourceManager(Long employeeId, ChangeResourceManagerRequest request);

    ResourceManagerResponsibilityResponse getCurrentResourceManagerResponsibility(Long employeeId);

    List<EmployeeUnderResourceManagerResponse> listEmployeesUnderResourceManager(Long resourceManagerId);
}
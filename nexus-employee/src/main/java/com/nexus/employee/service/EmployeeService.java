package com.nexus.employee.service;

import com.nexus.employee.dto.request.employee.CreateEmployeeRequest;
import com.nexus.employee.dto.request.employee.UpdateEmployeeInfoRequest;
import com.nexus.employee.dto.response.employee.EmployeePageResponse;
import com.nexus.employee.dto.response.employee.EmployeeResponse;

public interface EmployeeService {

    EmployeeResponse createEmployee(CreateEmployeeRequest request);

    EmployeePageResponse listEmployeesPage(String search, String role, int page, int size);

    EmployeeResponse getEmployeeDetails(Long employeeId);

    EmployeeResponse updateEmployeeInfo(Long employeeId, UpdateEmployeeInfoRequest request);

    EmployeeResponse getEmployeeByAuthUserId(Long authUserId);
}
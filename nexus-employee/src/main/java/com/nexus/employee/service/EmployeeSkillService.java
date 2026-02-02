package com.nexus.employee.service;

import com.nexus.employee.dto.request.employeeskill.AddEmployeeSkillsRequest;
import com.nexus.employee.dto.request.employeeskill.UpdateEmployeeSkillLevelRequest;
import com.nexus.employee.dto.response.employeeskill.EmployeeSkillResponse;

import java.util.List;

public interface EmployeeSkillService {

    List<EmployeeSkillResponse> addSkillsToEmployee(Long employeeId, AddEmployeeSkillsRequest request);

    EmployeeSkillResponse updateEmployeeSkillLevel(Long employeeId, Long employeeSkillId, UpdateEmployeeSkillLevelRequest request);

    void removeEmployeeSkill(Long employeeId, Long employeeSkillId);

    List<EmployeeSkillResponse> listEmployeeSkills(Long employeeId);
}

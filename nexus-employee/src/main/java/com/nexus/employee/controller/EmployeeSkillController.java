package com.nexus.employee.controller;

import com.nexus.employee.dto.request.employeeskill.AddEmployeeSkillsRequest;
import com.nexus.employee.dto.request.employeeskill.UpdateEmployeeSkillLevelRequest;
import com.nexus.employee.dto.response.employeeskill.EmployeeSkillResponse;
import com.nexus.employee.service.EmployeeSkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees/{employeeId}/skills")
public class EmployeeSkillController {

    private final EmployeeSkillService employeeSkillService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<EmployeeSkillResponse> addSkills(
            @PathVariable Long employeeId,
            @Valid @RequestBody AddEmployeeSkillsRequest request) {
        return employeeSkillService.addSkillsToEmployee(employeeId, request);
    }

    @PutMapping("/{employeeSkillId}")
    public EmployeeSkillResponse updateSkillLevel(
            @PathVariable Long employeeId,
            @PathVariable Long employeeSkillId,
            @Valid @RequestBody UpdateEmployeeSkillLevelRequest request) {
        return employeeSkillService.updateEmployeeSkillLevel(employeeId, employeeSkillId, request);
    }

    @DeleteMapping("/{employeeSkillId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSkill(
            @PathVariable Long employeeId,
            @PathVariable Long employeeSkillId) {
        employeeSkillService.removeEmployeeSkill(employeeId, employeeSkillId);
    }

    @GetMapping
    public List<EmployeeSkillResponse> listSkills(@PathVariable Long employeeId) {
        return employeeSkillService.listEmployeeSkills(employeeId);
    }
}

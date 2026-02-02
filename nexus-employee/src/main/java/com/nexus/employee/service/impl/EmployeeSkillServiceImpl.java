package com.nexus.employee.service.impl;

import com.nexus.employee.dto.request.employeeskill.AddEmployeeSkillRequest;
import com.nexus.employee.dto.request.employeeskill.AddEmployeeSkillsRequest;
import com.nexus.employee.dto.request.employeeskill.UpdateEmployeeSkillLevelRequest;
import com.nexus.employee.dto.response.employeeskill.EmployeeSkillResponse;
import com.nexus.employee.exception.EmployeeConflictException;
import com.nexus.employee.exception.ResourceNotFoundException;
import com.nexus.employee.mapper.EmployeeSkillMapper;
import com.nexus.employee.model.Employee;
import com.nexus.employee.model.EmployeeSkill;
import com.nexus.employee.model.Skill;
import com.nexus.employee.repository.EmployeeRepository;
import com.nexus.employee.repository.EmployeeSkillRepository;
import com.nexus.employee.repository.SkillRepository;
import com.nexus.employee.service.EmployeeSkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeSkillServiceImpl implements EmployeeSkillService {

    private final EmployeeRepository employeeRepository;
    private final SkillRepository skillRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final EmployeeSkillMapper employeeSkillMapper;

    @Override
    @Transactional
    public List<EmployeeSkillResponse> addSkillsToEmployee(Long employeeId, AddEmployeeSkillsRequest request) {
        Employee employee = getEmployee(employeeId);

        return request.skills().stream().map(item -> assignSkill(employee, item)).toList();
    }

    private EmployeeSkillResponse assignSkill(Employee employee, AddEmployeeSkillRequest item) {
        Skill skill = getSkill(item.skillId());

        if (!skill.isActive()) {
            throw new EmployeeConflictException("Skill '" + skill.getName() + "' is inactive and cannot be assigned");
        }

        if (employeeSkillRepository.existsByEmployeeIdAndSkillId(employee.getId(), skill.getId())) {
            throw new EmployeeConflictException("Employee already has skill '" + skill.getName() + "'");
        }

        EmployeeSkill employeeSkill = EmployeeSkill.builder()
                .employee(employee)
                .skill(skill)
                .level(item.level().trim())
                .build();

        return employeeSkillMapper.toResponse(employeeSkillRepository.save(employeeSkill));
    }

    @Override
    @Transactional
    public EmployeeSkillResponse updateEmployeeSkillLevel(Long employeeId, Long employeeSkillId, UpdateEmployeeSkillLevelRequest request) {
        getEmployee(employeeId);

        EmployeeSkill employeeSkill = getEmployeeSkill(employeeId, employeeSkillId);
        employeeSkill.setLevel(request.level().trim());

        return employeeSkillMapper.toResponse(employeeSkillRepository.save(employeeSkill));
    }

    @Override
    @Transactional
    public void removeEmployeeSkill(Long employeeId, Long employeeSkillId) {
        getEmployee(employeeId);

        EmployeeSkill employeeSkill = getEmployeeSkill(employeeId, employeeSkillId);
        employeeSkillRepository.delete(employeeSkill);
    }

    @Override
    public List<EmployeeSkillResponse> listEmployeeSkills(Long employeeId) {
        getEmployee(employeeId);

        return employeeSkillRepository.findAllByEmployeeIdOrderByIdAsc(employeeId)
                .stream()
                .map(employeeSkillMapper::toResponse)
                .toList();
    }

    private Employee getEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id " + employeeId));
    }

    private Skill getSkill(Long skillId) {
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id " + skillId));
    }

    private EmployeeSkill getEmployeeSkill(Long employeeId, Long employeeSkillId) {
        return employeeSkillRepository.findByIdAndEmployeeId(employeeSkillId, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Skill entry not found with id " + employeeSkillId + " for employee " + employeeId));
    }
}

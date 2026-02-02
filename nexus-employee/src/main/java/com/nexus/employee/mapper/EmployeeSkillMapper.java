package com.nexus.employee.mapper;

import com.nexus.employee.dto.response.employeeskill.EmployeeSkillResponse;
import com.nexus.employee.model.EmployeeSkill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployeeSkillMapper {

    @Mapping(target = "skillId", source = "skill.id")
    @Mapping(target = "skillName", source = "skill.name")
    @Mapping(target = "skillCategory", source = "skill.category")
    EmployeeSkillResponse toResponse(EmployeeSkill employeeSkill);
}

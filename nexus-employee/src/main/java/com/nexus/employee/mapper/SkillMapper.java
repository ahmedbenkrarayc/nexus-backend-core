package com.nexus.employee.mapper;

import com.nexus.employee.dto.request.skill.CreateSkillRequest;
import com.nexus.employee.dto.request.skill.UpdateSkillRequest;
import com.nexus.employee.dto.response.skill.SkillResponse;
import com.nexus.employee.model.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SkillMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "employeeSkills", ignore = true)
    Skill toSkill(CreateSkillRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "employeeSkills", ignore = true)
    void updateSkill(UpdateSkillRequest request, @MappingTarget Skill skill);

    SkillResponse toResponse(Skill skill);
}
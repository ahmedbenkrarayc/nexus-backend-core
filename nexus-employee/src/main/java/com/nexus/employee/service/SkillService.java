package com.nexus.employee.service;

import com.nexus.employee.dto.request.skill.CreateSkillRequest;
import com.nexus.employee.dto.request.skill.UpdateSkillRequest;
import com.nexus.employee.dto.response.skill.SkillResponse;

import java.util.List;

public interface SkillService {

    SkillResponse createSkill(CreateSkillRequest request);

    List<SkillResponse> listSkills();

    List<SkillResponse> listActiveSkills();

    SkillResponse getSkillDetails(Long skillId);

    SkillResponse updateSkill(Long skillId, UpdateSkillRequest request);

    void deleteSkill(Long skillId);
}
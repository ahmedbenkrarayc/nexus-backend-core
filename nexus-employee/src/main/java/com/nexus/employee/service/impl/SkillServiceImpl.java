package com.nexus.employee.service.impl;

import com.nexus.employee.dto.request.skill.CreateSkillRequest;
import com.nexus.employee.dto.request.skill.UpdateSkillRequest;
import com.nexus.employee.dto.response.skill.SkillResponse;
import com.nexus.employee.exception.EmployeeConflictException;
import com.nexus.employee.exception.ResourceNotFoundException;
import com.nexus.employee.mapper.SkillMapper;
import com.nexus.employee.model.Skill;
import com.nexus.employee.repository.SkillRepository;
import com.nexus.employee.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    @Override
    @Transactional
    public SkillResponse createSkill(CreateSkillRequest request) {
        validateDuplicateSkill(request.name(), request.category(), null);
        Skill skill = skillMapper.toSkill(request);
        return skillMapper.toResponse(skillRepository.save(skill));
    }

    @Override
    public List<SkillResponse> listSkills() {
        return skillRepository.findAllByOrderByIdAsc()
                .stream()
                .map(skillMapper::toResponse)
                .toList();
    }

    @Override
    public List<SkillResponse> listActiveSkills() {
        return skillRepository.findAllByActiveTrueOrderByIdAsc()
                .stream()
                .map(skillMapper::toResponse)
                .toList();
    }

    @Override
    public SkillResponse getSkillDetails(Long skillId) {
        return skillMapper.toResponse(getSkill(skillId));
    }

    @Override
    @Transactional
    public SkillResponse updateSkill(Long skillId, UpdateSkillRequest request) {
        Skill skill = getSkill(skillId);
        validateDuplicateSkill(request.name(), request.category(), skillId);
        skillMapper.updateSkill(request, skill);
        return skillMapper.toResponse(skillRepository.save(skill));
    }

    @Override
    @Transactional
    public void deleteSkill(Long skillId) {
        Skill skill = getSkill(skillId);
        skillRepository.delete(skill);
    }

    private Skill getSkill(Long skillId) {
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id " + skillId));
    }

    private void validateDuplicateSkill(String name, String category, Long skillId) {
        boolean exists = skillId == null
                ? skillRepository.existsByNameIgnoreCaseAndCategoryIgnoreCase(name, category)
                : skillRepository.existsByNameIgnoreCaseAndCategoryIgnoreCaseAndIdNot(name, category, skillId);

        if (exists) {
            throw new EmployeeConflictException("Skill with same name and category already exists");
        }
    }
}
package com.nexus.employee.service.impl;

import com.nexus.employee.dto.request.skill.CreateSkillRequest;
import com.nexus.employee.dto.request.skill.UpdateSkillRequest;
import com.nexus.employee.dto.response.skill.SkillResponse;
import com.nexus.employee.exception.EmployeeConflictException;
import com.nexus.employee.exception.ResourceNotFoundException;
import com.nexus.employee.mapper.SkillMapper;
import com.nexus.employee.model.Skill;
import com.nexus.employee.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkillServiceImplTest {

    @Mock
    SkillRepository skillRepository;

    @Mock
    SkillMapper skillMapper;

    @InjectMocks
    SkillServiceImpl skillService;

    Skill skill;
    SkillResponse skillResponse;

    @BeforeEach
    void setUp() {
        skill = Skill.builder()
                .id(10L)
                .name("Java")
                .category("Backend")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        skillResponse = new SkillResponse(10L, "Java", "Backend", true, skill.getCreatedAt(), skill.getUpdatedAt());
    }

    @Test
    void createSkill_persistsAndReturnsMappedResponse() {
        var request = new CreateSkillRequest("Kotlin", "Backend");
        when(skillRepository.existsByNameIgnoreCaseAndCategoryIgnoreCase("Kotlin", "Backend")).thenReturn(false);
        when(skillMapper.toSkill(request)).thenReturn(skill);
        when(skillRepository.save(skill)).thenReturn(skill);
        when(skillMapper.toResponse(skill)).thenReturn(skillResponse);

        SkillResponse result = skillService.createSkill(request);

        assertThat(result).isEqualTo(skillResponse);
        verify(skillRepository).save(skill);
    }

    @Test
    void createSkill_whenDuplicate_throwsConflict() {
        var request = new CreateSkillRequest("Java", "Backend");
        when(skillRepository.existsByNameIgnoreCaseAndCategoryIgnoreCase("Java", "Backend")).thenReturn(true);

        assertThatThrownBy(() -> skillService.createSkill(request))
                .isInstanceOf(EmployeeConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void getSkillDetails_whenMissing_throwsNotFound() {
        when(skillRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.getSkillDetails(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getSkillDetails_returnsMappedSkill() {
        when(skillRepository.findById(10L)).thenReturn(Optional.of(skill));
        when(skillMapper.toResponse(skill)).thenReturn(skillResponse);

        assertThat(skillService.getSkillDetails(10L)).isEqualTo(skillResponse);
    }

    @Test
    void listActiveSkills_mapsRepositoryResults() {
        when(skillRepository.findAllByActiveTrueOrderByIdAsc()).thenReturn(List.of(skill));
        when(skillMapper.toResponse(skill)).thenReturn(skillResponse);

        assertThat(skillService.listActiveSkills()).containsExactly(skillResponse);
    }

    @Test
    void updateSkill_whenDuplicateForAnotherId_throwsConflict() {
        var request = new UpdateSkillRequest("Go", "Backend", true);
        when(skillRepository.findById(10L)).thenReturn(Optional.of(skill));
        when(skillRepository.existsByNameIgnoreCaseAndCategoryIgnoreCaseAndIdNot("Go", "Backend", 10L)).thenReturn(true);

        assertThatThrownBy(() -> skillService.updateSkill(10L, request))
                .isInstanceOf(EmployeeConflictException.class);
    }

    @Test
    void updateSkill_persistsChanges() {
        var request = new UpdateSkillRequest("Scala", "Backend", true);
        when(skillRepository.findById(10L)).thenReturn(Optional.of(skill));
        when(skillRepository.existsByNameIgnoreCaseAndCategoryIgnoreCaseAndIdNot("Scala", "Backend", 10L))
                .thenReturn(false);
        when(skillRepository.save(skill)).thenReturn(skill);
        when(skillMapper.toResponse(skill)).thenReturn(skillResponse);

        assertThat(skillService.updateSkill(10L, request)).isEqualTo(skillResponse);
        verify(skillMapper).updateSkill(eq(request), eq(skill));
        verify(skillRepository).save(skill);
    }

    @Test
    void deleteSkill_removesWhenPresent() {
        when(skillRepository.findById(10L)).thenReturn(Optional.of(skill));

        skillService.deleteSkill(10L);

        verify(skillRepository).delete(skill);
    }
}

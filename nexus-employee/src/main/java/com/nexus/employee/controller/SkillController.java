package com.nexus.employee.controller;

import com.nexus.employee.dto.request.skill.CreateSkillRequest;
import com.nexus.employee.dto.request.skill.UpdateSkillRequest;
import com.nexus.employee.dto.response.skill.SkillResponse;
import com.nexus.employee.service.SkillService;
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
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SkillResponse createSkill(@Valid @RequestBody CreateSkillRequest request) {
        return skillService.createSkill(request);
    }

    @GetMapping
    public List<SkillResponse> listSkills() {
        return skillService.listSkills();
    }

    @GetMapping("/active")
    public List<SkillResponse> listActiveSkills() {
        return skillService.listActiveSkills();
    }

    @GetMapping("/{skillId}")
    public SkillResponse getSkillDetails(@PathVariable Long skillId) {
        return skillService.getSkillDetails(skillId);
    }

    @PutMapping("/{skillId}")
    public SkillResponse updateSkill(@PathVariable Long skillId, @Valid @RequestBody UpdateSkillRequest request) {
        return skillService.updateSkill(skillId, request);
    }

    @DeleteMapping("/{skillId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSkill(@PathVariable Long skillId) {
        skillService.deleteSkill(skillId);
    }
}
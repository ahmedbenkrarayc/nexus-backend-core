package com.nexus.project.controller;

import com.nexus.project.dto.request.CreateProjectRequest;
import com.nexus.project.dto.request.UpdateProjectRequest;
import com.nexus.project.dto.response.ProjectPageResponse;
import com.nexus.project.dto.response.ProjectResponse;
import com.nexus.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProjectDetails(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProjectDetails(projectId));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(projectId, request));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ProjectPageResponse> listMyProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(projectService.listMyProjects(page, size));
    }
}

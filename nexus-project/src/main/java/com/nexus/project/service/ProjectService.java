package com.nexus.project.service;

import com.nexus.project.dto.request.CreateProjectRequest;
import com.nexus.project.dto.request.UpdateProjectRequest;
import com.nexus.project.dto.response.ProjectPageResponse;
import com.nexus.project.dto.response.ProjectResponse;

public interface ProjectService {
    ProjectResponse createProject(CreateProjectRequest request);

    ProjectResponse getProjectDetails(Long projectId);

    ProjectResponse updateProject(Long projectId, UpdateProjectRequest request);

    void deleteProject(Long projectId);

    ProjectPageResponse listMyProjects(int page, int size);
}

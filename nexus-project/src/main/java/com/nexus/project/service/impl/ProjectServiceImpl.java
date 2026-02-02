package com.nexus.project.service.impl;

import com.nexus.employee.service.EmployeeService;
import com.nexus.project.dto.request.CreateProjectRequest;
import com.nexus.project.dto.request.UpdateProjectRequest;
import com.nexus.project.dto.response.ProjectPageResponse;
import com.nexus.project.dto.response.ProjectResponse;
import com.nexus.project.exception.ProjectConflictException;
import com.nexus.project.exception.ResourceNotFoundException;
import com.nexus.project.model.Project;
import com.nexus.project.model.enums.ProjectStatus;
import com.nexus.project.repository.ProjectRepository;
import com.nexus.project.service.ProjectService;
import com.nexus.shared.security.context.CurrentUserContext;
import com.nexus.shared.security.provider.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private static final String ROLE_PROJECT_MANAGER = "PROJECT_MANAGER";

    private final ProjectRepository projectRepository;
    private final CurrentUserProvider currentUserProvider;
    private final EmployeeService employeeService;

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        requireRole(caller, "Only project managers can create projects", ROLE_PROJECT_MANAGER);

        if (projectRepository.existsByNameIgnoreCase(request.name())) {
            throw new ProjectConflictException("Project name already exists");
        }

        Project project = new Project();
        project.setName(request.name());
        project.setDescription(
                buildProjectDescription(request.description(), request.businessContext(), request.client()));
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        project.setStatus(ProjectStatus.PLANNED);
        project.setOwnerCampusId(resolveOwnerCampusId(caller));
        project.setSponsorEmployeeId(caller.userId());

        return toResponse(projectRepository.save(project));
    }

    @Override
    public ProjectResponse getProjectDetails(Long projectId) {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        requireRole(caller, "Only project managers can view projects", ROLE_PROJECT_MANAGER);

        Project project = projectRepository.findByIdAndSponsorEmployeeId(projectId, caller.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + projectId));

        return toResponse(project);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request) {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        requireRole(caller, "Only project managers can update projects", ROLE_PROJECT_MANAGER);

        Project project = projectRepository.findByIdAndSponsorEmployeeId(projectId, caller.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + projectId));

        if (!project.getName().equalsIgnoreCase(request.name())
                && projectRepository.existsByNameIgnoreCase(request.name())) {
            throw new ProjectConflictException("Project name already exists");
        }

        project.setName(request.name());
        project.setDescription(request.description());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        project.setStatus(request.status());

        return toResponse(projectRepository.save(project));
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId) {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        requireRole(caller, "Only project managers can delete projects", ROLE_PROJECT_MANAGER);

        Project project = projectRepository.findByIdAndSponsorEmployeeId(projectId, caller.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + projectId));

        projectRepository.delete(project);
    }

    @Override
    public ProjectPageResponse listMyProjects(int page, int size) {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        requireRole(caller, "Only project managers can list projects", ROLE_PROJECT_MANAGER);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Project> projectPage = projectRepository.findBySponsorEmployeeId(caller.userId(), pageable);

        return new ProjectPageResponse(
                projectPage.getContent().stream().map(this::toResponse).toList(),
                projectPage.getNumber(),
                projectPage.getSize(),
                projectPage.getTotalElements(),
                projectPage.getTotalPages(),
                projectPage.isFirst(),
                projectPage.isLast());
    }

    private Long resolveOwnerCampusId(CurrentUserContext caller) {
        return employeeService.getEmployeeByAuthUserId(caller.userId()).campusId();
    }

    private void requireRole(CurrentUserContext caller, String message, String... roles) {
        if (caller == null || !caller.hasAnyRole(roles)) {
            throw forbidden(message);
        }
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }

    private String buildProjectDescription(String description, String businessContext, String client) {
        StringBuilder content = new StringBuilder();

        if (description != null && !description.isBlank()) {
            content.append(description.trim());
        }

        if (businessContext != null && !businessContext.isBlank()) {
            if (!content.isEmpty()) {
                content.append("\n\n");
            }
            content.append("Business context: ").append(businessContext.trim());
        }

        if (client != null && !client.isBlank()) {
            if (!content.isEmpty()) {
                content.append("\n");
            }
            content.append("Client: ").append(client.trim());
        }

        return content.isEmpty() ? null : content.toString();
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStartDate(),
                project.getEndDate(),
                project.getOwnerCampusId(),
                project.getSponsorEmployeeId(),
                project.getStatus(),
                project.getCreatedAt(),
                project.getUpdatedAt());
    }
}

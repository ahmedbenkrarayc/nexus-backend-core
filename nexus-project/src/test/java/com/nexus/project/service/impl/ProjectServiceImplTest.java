package com.nexus.project.service.impl;

import com.nexus.employee.dto.response.employee.EmployeeResponse;
import com.nexus.employee.service.EmployeeService;
import com.nexus.project.dto.request.CreateProjectRequest;
import com.nexus.project.dto.request.UpdateProjectRequest;
import com.nexus.project.dto.response.ProjectPageResponse;
import com.nexus.project.exception.ProjectConflictException;
import com.nexus.project.exception.ResourceNotFoundException;
import com.nexus.project.model.Project;
import com.nexus.project.model.enums.ProjectStatus;
import com.nexus.project.repository.ProjectRepository;
import com.nexus.shared.security.context.CurrentUserContext;
import com.nexus.shared.security.provider.CurrentUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    ProjectRepository projectRepository;

    @Mock
    CurrentUserProvider currentUserProvider;

    @Mock
    EmployeeService employeeService;

    @InjectMocks
    ProjectServiceImpl projectService;

    @Test
    void createProject_asProjectManager_savesAndReturnsResponse() {
        var caller = new CurrentUserContext(10L, Set.of("PROJECT_MANAGER"));
        when(currentUserProvider.getCurrentUser()).thenReturn(caller);
        when(projectRepository.existsByNameIgnoreCase("Alpha")).thenReturn(false);
        when(employeeService.getEmployeeByAuthUserId(10L)).thenReturn(
                new EmployeeResponse(1L, "A", "B", "C", "e@x.com", 10L, 99L, null, null));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project p = invocation.getArgument(0);
            p.setId(42L);
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            return p;
        });

        var request = new CreateProjectRequest("Alpha", "desc", LocalDate.of(2026, 1, 1), null, null, null);
        var response = projectService.createProject(request);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.name()).isEqualTo("Alpha");
        assertThat(response.ownerCampusId()).isEqualTo(99L);
        assertThat(response.sponsorEmployeeId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(ProjectStatus.PLANNED);
    }

    @Test
    void createProject_whenNameExists_throwsConflict() {
        when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUserContext(1L, Set.of("PROJECT_MANAGER")));
        when(projectRepository.existsByNameIgnoreCase("Dup")).thenReturn(true);

        var request = new CreateProjectRequest("Dup", null, LocalDate.now(), null, null, null);
        assertThatThrownBy(() -> projectService.createProject(request))
                .isInstanceOf(ProjectConflictException.class);
    }

    @Test
    void createProject_withoutRole_throwsForbidden() {
        when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUserContext(1L, Set.of("EMPLOYEE")));
        var request = new CreateProjectRequest("X", null, LocalDate.now(), null, null, null);

        assertThatThrownBy(() -> projectService.createProject(request))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void getProjectDetails_whenMissing_throwsNotFound() {
        when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUserContext(5L, Set.of("PROJECT_MANAGER")));
        when(projectRepository.findByIdAndSponsorEmployeeId(100L, 5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectDetails(100L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateProject_whenNewNameConflicts_throws() {
        Project existing = projectWithId(1L, "Old", ProjectStatus.PLANNED);
        when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUserContext(7L, Set.of("PROJECT_MANAGER")));
        when(projectRepository.findByIdAndSponsorEmployeeId(1L, 7L)).thenReturn(Optional.of(existing));
        when(projectRepository.existsByNameIgnoreCase("Taken")).thenReturn(true);

        var request = new UpdateProjectRequest("Taken", "d", LocalDate.now(), null, ProjectStatus.ACTIVE);
        assertThatThrownBy(() -> projectService.updateProject(1L, request))
                .isInstanceOf(ProjectConflictException.class);
    }

    @Test
    void listMyProjects_returnsPagedWrapper() {
        when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUserContext(3L, Set.of("PROJECT_MANAGER")));
        Project p = projectWithId(5L, "P", ProjectStatus.ACTIVE);
        when(projectRepository.findBySponsorEmployeeId(eq(3L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(p)));

        ProjectPageResponse page = projectService.listMyProjects(0, 10);

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().getFirst().name()).isEqualTo("P");
        assertThat(page.totalElements()).isEqualTo(1);
    }

    private static Project projectWithId(long id, String name, ProjectStatus status) {
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        project.setDescription("d");
        project.setStartDate(LocalDate.now());
        project.setOwnerCampusId(1L);
        project.setSponsorEmployeeId(3L);
        project.setStatus(status);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        return project;
    }
}

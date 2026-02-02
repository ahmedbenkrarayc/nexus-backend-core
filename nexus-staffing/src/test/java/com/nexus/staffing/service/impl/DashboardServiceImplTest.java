package com.nexus.staffing.service.impl;

import com.nexus.employee.model.Employee;
import com.nexus.employee.model.EmployeeSkill;
import com.nexus.employee.model.Skill;
import com.nexus.employee.repository.AbsenseRepository;
import com.nexus.employee.repository.EmployeeRepository;
import com.nexus.employee.repository.EmployeeResponsibilityRepository;
import com.nexus.employee.repository.EmployeeSkillRepository;
import com.nexus.organization.repository.CampusRepository;
import com.nexus.organization.repository.OrganizationRepository;
import com.nexus.project.model.Project;
import com.nexus.project.model.enums.ProjectStatus;
import com.nexus.project.repository.ProjectRepository;
import com.nexus.shared.security.context.CurrentUserContext;
import com.nexus.shared.security.provider.CurrentUserProvider;
import com.nexus.staffing.dto.response.statistics.DashboardStatisticsResponse;
import com.nexus.staffing.repository.AllocationRequestRepository;
import com.nexus.staffing.repository.EngagementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    CurrentUserProvider currentUserProvider;
    @Mock
    EmployeeRepository employeeRepository;
    @Mock
    ProjectRepository projectRepository;
    @Mock
    CampusRepository campusRepository;
    @Mock
    OrganizationRepository organizationRepository;
    @Mock
    AllocationRequestRepository allocationRequestRepository;
    @Mock
    EngagementRepository engagementRepository;
    @Mock
    AbsenseRepository absenseRepository;
    @Mock
    EmployeeSkillRepository employeeSkillRepository;
    @Mock
    EmployeeResponsibilityRepository responsibilityRepository;

    @InjectMocks
    DashboardServiceImpl dashboardService;

    @Test
    void getStatistics_asAdmin_buildsAdminSnapshot() {
        when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUserContext(1L, Set.of("ADMIN")));
        when(employeeRepository.findByAuthUser(1L)).thenReturn(Optional.empty());

        Project p = Project.builder().status(ProjectStatus.PLANNED).build();
        when(projectRepository.findAll()).thenReturn(List.of(p));
        when(employeeSkillRepository.findAll()).thenReturn(List.of());
        when(employeeRepository.count()).thenReturn(8L);
        when(projectRepository.count()).thenReturn(4L);
        when(campusRepository.count()).thenReturn(2L);
        when(organizationRepository.count()).thenReturn(1L);

        DashboardStatisticsResponse stats = dashboardService.getStatistics();

        assertThat(stats.adminStats()).isNotNull();
        assertThat(stats.adminStats().totalEmployees()).isEqualTo(8L);
        assertThat(stats.adminStats().totalProjects()).isEqualTo(4L);
        assertThat(stats.adminStats().projectsByStatus()).containsEntry("PLANNED", 1L);
        assertThat(stats.hrStats()).isNull();
    }

    @Test
    void getStatistics_asHr_buildsHrSnapshot() {
        when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUserContext(2L, Set.of("HR_MANAGER")));
        when(employeeRepository.findByAuthUser(2L)).thenReturn(Optional.empty());

        when(campusRepository.findAll()).thenReturn(List.of());
        when(employeeRepository.findAll()).thenReturn(List.of());
        when(employeeSkillRepository.findAll()).thenReturn(List.of());
        when(absenseRepository.findAll()).thenReturn(List.of());
        when(employeeRepository.count()).thenReturn(0L);

        DashboardStatisticsResponse stats = dashboardService.getStatistics();

        assertThat(stats.hrStats()).isNotNull();
        assertThat(stats.hrStats().totalEmployees()).isEqualTo(0L);
        assertThat(stats.adminStats()).isNull();
    }

    @Test
    void getStatistics_asEmployee_withLinkedEmployee_buildsEmployeeStats() {
        Employee employee = Employee.builder().id(20L).campusId(3L).build();
        when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUserContext(5L, Set.of("EMPLOYEE")));
        when(employeeRepository.findByAuthUser(5L)).thenReturn(Optional.of(employee));

        when(engagementRepository.findByEmployeeId(20L)).thenReturn(List.of());
        when(allocationRequestRepository.findByIdInOrderByCreatedAtDesc(List.of())).thenReturn(List.of());
        when(projectRepository.findAllById(List.of())).thenReturn(List.of());
        when(absenseRepository.findByEmployeeId(20L)).thenReturn(List.of());
        when(employeeSkillRepository.findAllByEmployeeIdOrderByIdAsc(20L)).thenReturn(List.of());

        DashboardStatisticsResponse stats = dashboardService.getStatistics();

        assertThat(stats.employeeStats()).isNotNull();
        assertThat(stats.employeeStats().activeProjects()).isZero();
    }

    @Test
    void getStatistics_adminCountsDistinctSkillsAcrossAssignments() {
        when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUserContext(1L, Set.of("ADMIN")));
        when(employeeRepository.findByAuthUser(1L)).thenReturn(Optional.empty());

        Skill java = Skill.builder().id(1L).name("Java").category("Lang").build();
        Skill kotlin = Skill.builder().id(2L).name("Kotlin").category("Lang").build();
        EmployeeSkill es1 = EmployeeSkill.builder().skill(java).build();
        EmployeeSkill es2 = EmployeeSkill.builder().skill(kotlin).build();

        when(projectRepository.findAll()).thenReturn(List.of());
        when(employeeSkillRepository.findAll()).thenReturn(List.of(es1, es2));
        when(employeeRepository.count()).thenReturn(0L);
        when(projectRepository.count()).thenReturn(0L);
        when(campusRepository.count()).thenReturn(0L);
        when(organizationRepository.count()).thenReturn(0L);

        DashboardStatisticsResponse stats = dashboardService.getStatistics();

        assertThat(stats.adminStats().totalSkillsCount()).isEqualTo(2L);
    }
}

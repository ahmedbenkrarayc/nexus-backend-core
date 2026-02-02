package com.nexus.staffing.service.impl;

import com.nexus.employee.model.Employee;
import com.nexus.employee.repository.AbsenseRepository;
import com.nexus.employee.repository.EmployeeRepository;
import com.nexus.employee.repository.EmployeeResponsibilityRepository;
import com.nexus.employee.repository.EmployeeSkillRepository;
import com.nexus.organization.model.Campus;
import com.nexus.organization.repository.CampusRepository;
import com.nexus.organization.repository.OrganizationRepository;
import com.nexus.project.model.Project;
import com.nexus.project.repository.ProjectRepository;
import com.nexus.shared.security.context.CurrentUserContext;
import com.nexus.shared.security.provider.CurrentUserProvider;
import com.nexus.staffing.dto.response.statistics.DashboardStatisticsResponse;
import com.nexus.staffing.model.AllocationRequest;
import com.nexus.staffing.model.Engagement;
import com.nexus.staffing.model.EngagementDecision;
import com.nexus.staffing.model.enums.EngagementDecisionType;
import com.nexus.staffing.repository.AllocationRequestRepository;
import com.nexus.staffing.repository.EngagementDecisionRepository;
import com.nexus.staffing.repository.EngagementRepository;
import com.nexus.staffing.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final CurrentUserProvider currentUserProvider;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final CampusRepository campusRepository;
    private final OrganizationRepository organizationRepository;
    private final AllocationRequestRepository allocationRequestRepository;
    private final EngagementRepository engagementRepository;
    private final EngagementDecisionRepository engagementDecisionRepository;
    private final AbsenseRepository absenseRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final EmployeeResponsibilityRepository responsibilityRepository;

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_HR = "HR_MANAGER";
    private static final String ROLE_PM = "PROJECT_MANAGER";
    private static final String ROLE_RM = "RESOURCE_MANAGER";
    private static final String ROLE_EMPLOYEE = "EMPLOYEE";

    @Override
    public DashboardStatisticsResponse getStatistics() {
        CurrentUserContext caller = currentUserProvider.getCurrentUser();

        DashboardStatisticsResponse.AdminStats adminStats = null;
        DashboardStatisticsResponse.HrStats hrStats = null;
        DashboardStatisticsResponse.EmployeeStats employeeStats = null;
        DashboardStatisticsResponse.ProjectManagerStats pmStats = null;
        DashboardStatisticsResponse.ResourceManagerStats rmStats = null;

        if (caller.hasRole(ROLE_ADMIN)) {
            adminStats = calculateAdminStats();
        }
        if (caller.hasRole(ROLE_HR)) {
            hrStats = calculateHrStats();
        }

        Employee employee = employeeRepository.findByAuthUser(caller.userId()).orElse(null);
        if (employee != null) {
            if (caller.hasRole(ROLE_EMPLOYEE)) {
                employeeStats = calculateEmployeeStats(employee);
            }
            if (caller.hasRole(ROLE_PM)) {
                pmStats = calculatePmStats(employee);
            }
            if (caller.hasRole(ROLE_RM)) {
                rmStats = calculateRmStats(employee);
            }
        }

        return new DashboardStatisticsResponse(adminStats, hrStats, employeeStats, pmStats, rmStats);
    }

    private DashboardStatisticsResponse.AdminStats calculateAdminStats() {
        Map<String, Long> projectsByStatus = projectRepository.findAll().stream()
                .collect(Collectors.groupingBy(p -> p.getStatus().name(), Collectors.counting()));

        long totalSkillsCount = employeeSkillRepository.findAll().stream()
                .map(es -> es.getSkill().getId())
                .distinct()
                .count();

        return new DashboardStatisticsResponse.AdminStats(
                employeeRepository.count(),
                projectRepository.count(),
                campusRepository.count(),
                organizationRepository.count(),
                totalSkillsCount,
                projectsByStatus
        );
    }

    private DashboardStatisticsResponse.HrStats calculateHrStats() {
        Map<Long, String> campusNames = campusRepository.findAll().stream()
                .collect(Collectors.toMap(Campus::getId, Campus::getName, (a, b) -> a));

        Map<String, Long> employeesPerCampus = employeeRepository.findAll().stream()
                .collect(Collectors.groupingBy(e -> campusNames.getOrDefault(e.getCampusId(), "Unknown"), Collectors.counting()));

        Map<String, Long> skillCounts = employeeSkillRepository.findAll().stream()
                .collect(Collectors.groupingBy(es -> es.getSkill().getName(), Collectors.counting()));

        List<DashboardStatisticsResponse.SkillCount> topSkills = skillCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> new DashboardStatisticsResponse.SkillCount(e.getKey(), e.getValue()))
                .toList();

        LocalDate today = LocalDate.now();
        long activeAbsencesCount = absenseRepository.findAll().stream()
                .filter(a -> a.isApproved() && !today.isBefore(a.getStart()) && !today.isAfter(a.getEnd()))
                .count();

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long recentHiresCount = employeeRepository.findAll().stream()
                .filter(e -> e.getCreatedAt().isAfter(thirtyDaysAgo))
                .count();

        Map<String, Long> absencesByType = absenseRepository.findAll().stream()
                .collect(Collectors.groupingBy(com.nexus.employee.model.Absense::getType, Collectors.counting()));

        return new DashboardStatisticsResponse.HrStats(
                employeeRepository.count(),
                activeAbsencesCount,
                recentHiresCount,
                employeesPerCampus,
                absencesByType,
                topSkills
        );
    }

    private DashboardStatisticsResponse.EmployeeStats calculateEmployeeStats(Employee employee) {
        var engagements = engagementRepository.findByEmployeeId(employee.getId());
        
        List<Long> allocationRequestIds = engagements.stream().map(com.nexus.staffing.model.Engagement::getAllocationRequestId).toList();
        List<AllocationRequest> requests = allocationRequestRepository.findByIdInOrderByCreatedAtDesc(allocationRequestIds);
        List<Long> projectIds = requests.stream().map(AllocationRequest::getProjectId).distinct().toList();
        
        Map<String, Long> myProjectsByStatus = projectRepository.findAllById(projectIds).stream()
                .collect(Collectors.groupingBy(p -> p.getStatus().name(), Collectors.counting()));

        LocalDate today = LocalDate.now();
        long upcomingAbsencesCount = absenseRepository.findByEmployeeId(employee.getId()).stream()
                .filter(a -> a.isApproved() && a.getStart().isAfter(today))
                .count();

        return new DashboardStatisticsResponse.EmployeeStats(
                engagements.size(),
                employeeSkillRepository.findAllByEmployeeIdOrderByIdAsc(employee.getId()).size(),
                absenseRepository.findByEmployeeId(employee.getId()).stream().filter(com.nexus.employee.model.Absense::isApproved).count(),
                absenseRepository.findByEmployeeId(employee.getId()).stream().filter(a -> !a.isApproved()).count(),
                upcomingAbsencesCount,
                myProjectsByStatus
        );
    }

    private DashboardStatisticsResponse.ProjectManagerStats calculatePmStats(Employee pm) {
        // Project.sponsorEmployeeId stores the auth user id (see ProjectServiceImpl#createProject),
        // while Employee.id is the employee table PK. For PM stats we must query by Employee.authUser.
        List<Project> myProjects = projectRepository.findBySponsorEmployeeId(pm.getAuthUser());
        List<Long> projectIds = myProjects.stream().map(Project::getId).toList();
        List<AllocationRequest> requests = allocationRequestRepository.findByProjectIdInOrderByCreatedAtDesc(projectIds);
        List<Long> requestIds = requests.stream().map(AllocationRequest::getId).toList();
        List<Engagement> engagements = engagementRepository.findByAllocationRequestIdIn(requestIds);

        Map<String, Long> myProjectsByStatus = myProjects.stream()
                .collect(Collectors.groupingBy(p -> p.getStatus().name(), Collectors.counting()));

        long pendingRequestsCount = countPendingRequests(requests, engagements);

        long uniqueResourceCount = engagements.stream()
                .map(com.nexus.staffing.model.Engagement::getEmployeeId)
                .distinct()
                .count();

        return new DashboardStatisticsResponse.ProjectManagerStats(
                myProjects.size(),
                pendingRequestsCount,
                engagements.size(),
                uniqueResourceCount,
                myProjectsByStatus
        );
    }

    private long countPendingRequests(List<AllocationRequest> requests, List<Engagement> engagements) {
        if (requests.isEmpty()) {
            return 0;
        }

        Map<Long, List<Engagement>> engagementsByRequestId = engagements.stream()
                .collect(Collectors.groupingBy(Engagement::getAllocationRequestId));

        List<Long> engagementIds = engagements.stream().map(Engagement::getId).toList();
        Map<Long, List<EngagementDecision>> decisionsByEngagementId = engagementIds.isEmpty()
                ? Map.of()
                : engagementDecisionRepository.findByEngagementIdIn(engagementIds).stream()
                .collect(Collectors.groupingBy(EngagementDecision::getEngagementId));

        return requests.stream()
                .filter(request -> isPending(engagementsByRequestId.getOrDefault(request.getId(), List.of()), decisionsByEngagementId))
                .count();
    }

    private boolean isPending(List<Engagement> requestEngagements, Map<Long, List<EngagementDecision>> decisionsByEngagementId) {
        if (requestEngagements.isEmpty()) {
            return true;
        }

        List<EngagementDecision> decisions = requestEngagements.stream()
                .flatMap(engagement -> decisionsByEngagementId.getOrDefault(engagement.getId(), List.of()).stream())
                .toList();

        boolean hasApproved = decisions.stream()
                .anyMatch(decision -> decision.getDecision() == EngagementDecisionType.APPROVED);
        if (hasApproved) {
            return false;
        }

        boolean hasRejected = decisions.stream()
                .anyMatch(decision -> decision.getDecision() == EngagementDecisionType.REJECTED);
        return !hasRejected;
    }

    private DashboardStatisticsResponse.ResourceManagerStats calculateRmStats(Employee rm) {
        var responsibilities = responsibilityRepository.findAllByResponsibleIdAndTypeAndActiveTrueOrderByStartDateDesc(rm.getId(), "RESOURCE_MANAGER");
        long managedCount = responsibilities.size();
        
        double utilizationRate = 0;
        long benchCount = 0;
        List<DashboardStatisticsResponse.SkillCount> topTeamSkills = Collections.emptyList();

        if (managedCount > 0) {
            List<Long> managedEmployeeIds = responsibilities.stream().map(r -> r.getEmployee().getId()).toList();
            
            long utilizedCount = managedEmployeeIds.stream()
                    .filter(id -> !engagementRepository.findByEmployeeId(id).isEmpty())
                    .count();
            utilizationRate = (double) utilizedCount / managedCount;
            benchCount = managedCount - utilizedCount;

            Map<String, Long> teamSkillCounts = employeeSkillRepository.findAllByEmployeeIdIn(managedEmployeeIds).stream()
                    .collect(Collectors.groupingBy(es -> es.getSkill().getName(), Collectors.counting()));

            topTeamSkills = teamSkillCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(3)
                    .map(e -> new DashboardStatisticsResponse.SkillCount(e.getKey(), e.getValue()))
                    .toList();
        }

        return new DashboardStatisticsResponse.ResourceManagerStats(
                managedCount,
                allocationRequestRepository.count(),
                utilizationRate,
                benchCount,
                topTeamSkills
        );
    }
}

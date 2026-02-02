package com.nexus.staffing.dto.response.statistics;

import java.util.List;
import java.util.Map;

public record DashboardStatisticsResponse(
    AdminStats adminStats,
    HrStats hrStats,
    EmployeeStats employeeStats,
    ProjectManagerStats pmStats,
    ResourceManagerStats rmStats
) {
    public record AdminStats(
        long totalEmployees,
        long totalProjects,
        long totalCampuses,
        long totalUnits,
        long totalSkillsCount,
        Map<String, Long> projectsByStatus
    ) {}

    public record HrStats(
        long totalEmployees,
        long activeAbsences,
        long recentHiresCount,
        Map<String, Long> employeesPerCampus,
        Map<String, Long> absencesByType,
        List<SkillCount> topSkills
    ) {}

    public record EmployeeStats(
        long activeProjects,
        long skillCount,
        long totalAbsenceDays,
        long pendingRequests,
        long upcomingAbsencesCount,
        Map<String, Long> myProjectsByStatus
    ) {}

    public record ProjectManagerStats(
        long myActiveProjects,
        long pendingAllocationRequests,
        long totalResources,
        long uniqueResourceCount,
        Map<String, Long> myProjectsByStatus
    ) {}

    public record ResourceManagerStats(
        long managedEmployees,
        long activeRequests,
        double utilizationRate,
        long benchCount,
        List<SkillCount> topTeamSkills
    ) {}

    public record SkillCount(
        String name,
        Long count
    ) {}
}

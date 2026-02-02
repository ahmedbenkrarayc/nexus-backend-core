package com.nexus.staffing.mapper;

import com.nexus.employee.dto.response.absense.AbsenseResponse;
import com.nexus.employee.model.Absense;
import com.nexus.employee.model.Employee;
import com.nexus.project.model.Project;
import com.nexus.staffing.dto.response.allocationrequest.AllocationRequestTrackingStatus;
import com.nexus.staffing.dto.response.employee.EmployeeAllocationRequestResponse;
import com.nexus.staffing.dto.response.employee.EmployeeEngagementResponse;
import com.nexus.staffing.dto.response.employee.EmployeeProjectDetailsResponse;
import com.nexus.staffing.model.AllocationRequest;
import com.nexus.staffing.model.Engagement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface EmployeeSelfServiceMapper {

    @Mapping(source = "engagement.id", target = "engagementId")
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.name", target = "projectName")
    @Mapping(source = "projectCampus", target = "projectCampus")
    @Mapping(source = "engagement.roleOnProject", target = "roleOnProject")
    @Mapping(source = "engagement.engagementLevel", target = "engagementLevel")
    @Mapping(source = "engagement.startDate", target = "startDate")
    @Mapping(source = "engagement.endDate", target = "endDate")
    @Mapping(source = "engagement.status", target = "status")
    EmployeeEngagementResponse toEngagementResponse(Engagement engagement, Project project, String projectCampus);

    @Mapping(source = "project.id", target = "id")
    @Mapping(source = "project.name", target = "name")
    @Mapping(source = "project.description", target = "description")
    @Mapping(source = "ownerCampus", target = "ownerCampus")
    @Mapping(source = "project.startDate", target = "startDate")
    @Mapping(source = "project.endDate", target = "endDate")
    @Mapping(source = "project.status", target = "status")
    @Mapping(source = "myEngagement", target = "myEngagement")
    EmployeeProjectDetailsResponse toProjectDetailsResponse(Project project, String ownerCampus, EmployeeEngagementResponse myEngagement);

    @Mapping(source = "request.id", target = "requestId")
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.name", target = "projectName")
    @Mapping(source = "projectCampus", target = "projectCampus")
    @Mapping(source = "requestedRole", target = "requestedRole")
    @Mapping(source = "engagementLevel", target = "requestedEngagementLevel")
    @Mapping(source = "startDate", target = "requestedStartDate")
    @Mapping(source = "endDate", target = "requestedEndDate")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "requester.fname", target = "requesterName") // Simple first name or concat
    @Mapping(source = "userComment", target = "comment")
    EmployeeAllocationRequestResponse toAllocationRequestResponse(
            AllocationRequest request,
            Project project,
            String projectCampus,
            Employee requester,
            String requestedRole,
            String engagementLevel,
            LocalDate startDate,
            LocalDate endDate,
            String userComment,
            AllocationRequestTrackingStatus status);

    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "employee.fname", target = "employeeName")
    AbsenseResponse toAbsenceResponse(Absense absense);
}

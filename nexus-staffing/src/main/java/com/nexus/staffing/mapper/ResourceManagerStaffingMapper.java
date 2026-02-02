package com.nexus.staffing.mapper;

import com.nexus.employee.model.Absense;
import com.nexus.employee.model.Employee;
import com.nexus.employee.model.EmployeeSkill;
import com.nexus.project.model.Project;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerAllocationRequestDetailsResponse;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerAllocationRequestListItemResponse;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerEmployeeAbsenceResponse;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerEmployeeEngagementResponse;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerEmployeeListItemResponse;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerEmployeeSkillResponse;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerRequestConflictResponse;
import com.nexus.staffing.model.AllocationRequest;
import com.nexus.staffing.model.Engagement;
import com.nexus.staffing.model.EngagementDecision;
import com.nexus.staffing.model.enums.EngagementDecisionType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ResourceManagerStaffingMapper {

        @Mapping(source = "id", target = "employeeId")
        @Mapping(source = "code", target = "employeeCode")
        @Mapping(source = "fname", target = "firstName")
        @Mapping(source = "lname", target = "lastName")
        ResourceManagerEmployeeListItemResponse toEmployeeListItem(Employee employee);

        @Mapping(source = "skill.id", target = "skillId")
        @Mapping(source = "skill.name", target = "skillName")
        @Mapping(source = "skill.category", target = "skillCategory")
        ResourceManagerEmployeeSkillResponse toEmployeeSkillResponse(EmployeeSkill employeeSkill);

        @Mapping(source = "id", target = "absenceId")
        ResourceManagerEmployeeAbsenceResponse toEmployeeAbsenceResponse(Absense absense);

        @Mapping(source = "engagement.id", target = "engagementId")
        @Mapping(source = "engagement.allocationRequestId", target = "allocationRequestId")
        @Mapping(source = "projectId", target = "projectId")
        @Mapping(source = "projectName", target = "projectName")
        @Mapping(source = "engagement.roleOnProject", target = "roleOnProject")
        @Mapping(source = "engagement.engagementLevel", target = "engagementLevel")
        @Mapping(source = "engagement.startDate", target = "startDate")
        @Mapping(source = "engagement.endDate", target = "endDate")
        @Mapping(source = "engagement.status", target = "status")
        ResourceManagerEmployeeEngagementResponse toEmployeeEngagementResponse(
                        Engagement engagement,
                        Long projectId,
                        String projectName);

        @Mapping(source = "request.id", target = "requestId")
        @Mapping(source = "project.id", target = "projectId")
        @Mapping(source = "project.name", target = "projectName")
        @Mapping(source = "projectManager.id", target = "projectManagerEmployeeId")
        @Mapping(source = "projectManager.fname", target = "projectManagerFirstName")
        @Mapping(source = "projectManager.lname", target = "projectManagerLastName")
        @Mapping(source = "employee.id", target = "employeeId")
        @Mapping(source = "employee.fname", target = "employeeFirstName")
        @Mapping(source = "employee.lname", target = "employeeLastName")
        @Mapping(source = "requiredRole", target = "requiredRole")
        @Mapping(source = "engagementLevel", target = "engagementLevel")
        @Mapping(source = "requestedStartDate", target = "requestedStartDate")
        @Mapping(source = "requestedEndDate", target = "requestedEndDate")
        @Mapping(source = "request.createdAt", target = "createdAt")
        @Mapping(source = "status", target = "status")
        ResourceManagerAllocationRequestListItemResponse toAllocationRequestListItemResponse(
                        AllocationRequest request,
                        Project project,
                        Employee projectManager,
                        Employee employee,
                        String requiredRole,
                        String engagementLevel,
                        LocalDate requestedStartDate,
                        LocalDate requestedEndDate,
                        String status);

        @Mapping(source = "request.id", target = "requestId")
        @Mapping(source = "project.id", target = "projectId")
        @Mapping(source = "project.name", target = "projectName")
        @Mapping(source = "project.description", target = "projectDescription")
        @Mapping(source = "projectCampusName", target = "projectCampusName")
        @Mapping(source = "projectManager.id", target = "projectManagerEmployeeId")
        @Mapping(source = "projectManager.fname", target = "projectManagerFirstName")
        @Mapping(source = "projectManager.lname", target = "projectManagerLastName")
        @Mapping(source = "employee.id", target = "employeeId")
        @Mapping(source = "employee.code", target = "employeeCode")
        @Mapping(source = "employee.fname", target = "employeeFirstName")
        @Mapping(source = "employee.lname", target = "employeeLastName")
        @Mapping(source = "employee.email", target = "employeeEmail")
        @Mapping(source = "employee.campusId", target = "employeeCampusId")
        @Mapping(source = "requiredRole", target = "requiredRole")
        @Mapping(source = "requiredSkill", target = "requiredSkill")
        @Mapping(source = "engagementLevel", target = "engagementLevel")
        @Mapping(source = "requestedStartDate", target = "requestedStartDate")
        @Mapping(source = "requestedEndDate", target = "requestedEndDate")
        @Mapping(source = "comment", target = "comment")
        @Mapping(source = "status", target = "status")
        @Mapping(source = "request.createdAt", target = "createdAt")
        @Mapping(source = "request.updatedAt", target = "updatedAt")
        @Mapping(source = "overlappingEngagements", target = "overlappingEngagements")
        @Mapping(source = "overlappingAbsences", target = "overlappingAbsences")
        @Mapping(source = "conflict", target = "conflict")
        ResourceManagerAllocationRequestDetailsResponse toAllocationRequestDetailsResponse(
                        AllocationRequest request,
                        Project project,
                        Employee projectManager,
                        Employee employee,
                        String requiredRole,
                        String requiredSkill,
                        String engagementLevel,
                        LocalDate requestedStartDate,
                        LocalDate requestedEndDate,
                        String comment,
                        String status,
                        List<ResourceManagerEmployeeEngagementResponse> overlappingEngagements,
                        List<ResourceManagerEmployeeAbsenceResponse> overlappingAbsences,
                        ResourceManagerRequestConflictResponse conflict,
                        String projectCampusName);

        @Mapping(source = "engagement.id", target = "engagementId")
        @Mapping(source = "decidedByEmployeeId", target = "decidedBy")
        @Mapping(source = "decisionType", target = "decision")
        @Mapping(source = "comment", target = "comment")
        @Mapping(target = "id", ignore = true)
        @Mapping(target = "decisionDate", ignore = true)
        @Mapping(target = "createdAt", ignore = true)
        @Mapping(target = "updatedAt", ignore = true)
        EngagementDecision toEngagementDecision(
                        Engagement engagement,
                        Long decidedByEmployeeId,
                        EngagementDecisionType decisionType,
                        String comment);
}

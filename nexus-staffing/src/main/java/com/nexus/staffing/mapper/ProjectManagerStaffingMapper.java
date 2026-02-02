package com.nexus.staffing.mapper;

import com.nexus.employee.model.Employee;
import com.nexus.staffing.dto.response.allocationrequest.AllocationRequestResponse;
import com.nexus.staffing.dto.response.allocationrequest.AllocationRequestTrackingStatus;
import com.nexus.staffing.dto.response.engagement.ProjectTeamMemberResponse;
import com.nexus.staffing.model.AllocationRequest;
import com.nexus.staffing.model.Engagement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface ProjectManagerStaffingMapper {

    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "employee.fname", target = "firstName")
    @Mapping(source = "employee.lname", target = "lastName")
    @Mapping(source = "employee.campusId", target = "campusId")
    @Mapping(source = "engagement.roleOnProject", target = "roleOnProject")
    @Mapping(source = "engagement.engagementLevel", target = "engagementLevel")
    @Mapping(source = "engagement.startDate", target = "startDate")
    @Mapping(source = "engagement.endDate", target = "endDate")
    ProjectTeamMemberResponse toProjectTeamMemberResponse(Engagement engagement, Employee employee);

    @Mapping(source = "request.id", target = "id")
    @Mapping(source = "request.projectId", target = "projectId")
    @Mapping(source = "request.createdByEmployeeId", target = "createdByEmployeeId")
    @Mapping(source = "requiredRole", target = "requiredRole")
    @Mapping(source = "requiredSkill", target = "requiredSkill")
    @Mapping(source = "engagementLevel", target = "engagementLevel")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "userComment", target = "comment")
    @Mapping(source = "specificEmployeeId", target = "specificEmployeeId")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "request.createdAt", target = "createdAt")
    @Mapping(source = "request.updatedAt", target = "updatedAt")
    AllocationRequestResponse toAllocationRequestResponse(
            AllocationRequest request,
            String requiredRole,
            String requiredSkill,
            String engagementLevel,
            LocalDate startDate,
            LocalDate endDate,
            String userComment,
            Long specificEmployeeId,
            AllocationRequestTrackingStatus status);
}

package com.nexus.employee.mapper;

import com.nexus.employee.dto.response.responsibility.EmployeeUnderResourceManagerResponse;
import com.nexus.employee.dto.response.responsibility.ResourceManagerResponsibilityResponse;
import com.nexus.employee.model.EmployeeResponsibility;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployeeResponsibilityMapper {

    @Mapping(source = "id", target = "responsibilityId")
    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "employee.code", target = "employeeCode")
    @Mapping(source = "employee.fname", target = "employeeFirstName")
    @Mapping(source = "employee.lname", target = "employeeLastName")
    @Mapping(source = "responsible.id", target = "resourceManagerId")
    @Mapping(source = "responsible.code", target = "resourceManagerCode")
    @Mapping(source = "responsible.fname", target = "resourceManagerFirstName")
    @Mapping(source = "responsible.lname", target = "resourceManagerLastName")
    ResourceManagerResponsibilityResponse toResourceManagerResponse(EmployeeResponsibility responsibility);

    @Mapping(source = "id", target = "responsibilityId")
    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "employee.code", target = "employeeCode")
    @Mapping(source = "employee.fname", target = "employeeFirstName")
    @Mapping(source = "employee.lname", target = "employeeLastName")
    @Mapping(source = "employee.email", target = "employeeEmail")
    @Mapping(source = "employee.campusId", target = "campusId")
    @Mapping(source = "startDate", target = "assignedAt")
    EmployeeUnderResourceManagerResponse toEmployeeUnderResourceManagerResponse(EmployeeResponsibility responsibility);
}
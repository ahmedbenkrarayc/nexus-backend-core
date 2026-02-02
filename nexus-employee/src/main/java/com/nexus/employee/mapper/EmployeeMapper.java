package com.nexus.employee.mapper;

import com.nexus.employee.dto.request.employee.EmployeeInfoRequest;
import com.nexus.employee.dto.request.employee.UpdateEmployeeInfoRequest;
import com.nexus.employee.dto.response.employee.EmployeeListItemResponse;
import com.nexus.employee.dto.response.employee.EmployeeResponse;
import com.nexus.employee.model.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authUser", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "employeeSkills", ignore = true)
    @Mapping(target = "responsibilitiesAsEmployee", ignore = true)
    @Mapping(target = "responsibilitiesAsResponsible", ignore = true)
    @Mapping(target = "absenses", ignore = true)
    Employee toEmployee(EmployeeInfoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authUser", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "employeeSkills", ignore = true)
    @Mapping(target = "responsibilitiesAsEmployee", ignore = true)
    @Mapping(target = "responsibilitiesAsResponsible", ignore = true)
    @Mapping(target = "absenses", ignore = true)
    void updateEmployee(UpdateEmployeeInfoRequest request, @MappingTarget Employee employee);

    EmployeeResponse toResponse(Employee employee);

    EmployeeListItemResponse toListItem(Employee employee);
}
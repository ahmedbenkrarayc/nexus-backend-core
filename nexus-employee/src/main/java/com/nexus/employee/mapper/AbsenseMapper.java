package com.nexus.employee.mapper;

import com.nexus.employee.dto.request.absense.CreateAbsenseRequest;
import com.nexus.employee.dto.request.absense.UpdateAbsenseRequest;
import com.nexus.employee.dto.response.absense.AbsenseResponse;
import com.nexus.employee.model.Absense;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AbsenseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Absense toAbsense(CreateAbsenseRequest request);

    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "employee.fname", target = "employeeName")
    AbsenseResponse toResponse(Absense absense);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateAbsense(UpdateAbsenseRequest request, @MappingTarget Absense absense);
}

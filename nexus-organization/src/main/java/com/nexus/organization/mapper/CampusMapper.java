package com.nexus.organization.mapper;

import com.nexus.organization.dto.request.campus.CreateCampusRequest;
import com.nexus.organization.dto.request.campus.UpdateCampusRequest;
import com.nexus.organization.dto.request.location.LocationRequest;
import com.nexus.organization.dto.response.campus.CampusResponse;
import com.nexus.organization.dto.response.location.LocationResponse;
import com.nexus.organization.model.Campus;
import com.nexus.organization.model.Location;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CampusMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "organization", ignore = true)
    Campus toCampus(CreateCampusRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "location", ignore = true)
    void updateCampus(UpdateCampusRequest request, @MappingTarget Campus campus);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "campus", ignore = true)
    @Mapping(source = "longitude", target = "lang")
    @Mapping(source = "latitude", target = "lat")
    Location toLocation(LocationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "campus", ignore = true)
    @Mapping(source = "longitude", target = "lang")
    @Mapping(source = "latitude", target = "lat")
    void updateLocation(LocationRequest request, @MappingTarget Location location);

    @Mapping(source = "organization.id", target = "organizationId")
    @Mapping(source = "organization.name", target = "organizationName")
    CampusResponse toResponse(Campus campus);

    @Mapping(source = "lang", target = "longitude")
    @Mapping(source = "lat", target = "latitude")
    LocationResponse toResponse(Location location);
}
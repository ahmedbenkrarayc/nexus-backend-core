package com.nexus.organization.service.impl;

import com.nexus.organization.config.OrganizationDefaults;
import com.nexus.organization.dto.request.campus.CreateCampusRequest;
import com.nexus.organization.dto.request.location.LocationRequest;
import com.nexus.organization.dto.response.campus.CampusResponse;
import com.nexus.organization.exception.CampusConflictException;
import com.nexus.organization.exception.ResourceNotFoundException;
import com.nexus.organization.mapper.CampusMapper;
import com.nexus.organization.model.Campus;
import com.nexus.organization.model.Location;
import com.nexus.organization.model.Organization;
import com.nexus.organization.repository.CampusRepository;
import com.nexus.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampusServiceImplTest {

    @Mock
    CampusRepository campusRepository;

    @Mock
    OrganizationRepository organizationRepository;

    @Mock
    CampusMapper campusMapper;

    @InjectMocks
    CampusServiceImpl campusService;

    Organization defaultOrg;

    @BeforeEach
    void setUp() {
        defaultOrg = Organization.builder()
                .id(1L)
                .name(OrganizationDefaults.DEFAULT_ORGANIZATION_NAME)
                .code(OrganizationDefaults.DEFAULT_ORGANIZATION_CODE)
                .active(true)
                .build();
    }

    @Test
    void listCampuses_returnsMappedRowsForDefaultOrganization() {
        when(organizationRepository.findByCode(OrganizationDefaults.DEFAULT_ORGANIZATION_CODE))
                .thenReturn(Optional.of(defaultOrg));
        when(campusRepository.findAllByOrganizationIdOrderByIdAsc(1L)).thenReturn(List.of());

        assertThat(campusService.listCampuses()).isEmpty();
    }

    @Test
    void getCampusDetails_whenMissing_throwsNotFound() {
        when(organizationRepository.findByCode(OrganizationDefaults.DEFAULT_ORGANIZATION_CODE))
                .thenReturn(Optional.of(defaultOrg));
        when(campusRepository.findByIdAndOrganizationId(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> campusService.getCampusDetails(5L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("5");
    }

    @Test
    void getCampusDetails_returnsMappedCampus() {
        Campus campus = Campus.builder().id(3L).name("HQ").active(true).timezone("UTC").organization(defaultOrg).build();
        CampusResponse response = new CampusResponse(3L, "HQ", true, "UTC", 1L, "Org", null);

        when(organizationRepository.findByCode(OrganizationDefaults.DEFAULT_ORGANIZATION_CODE))
                .thenReturn(Optional.of(defaultOrg));
        when(campusRepository.findByIdAndOrganizationId(3L, 1L)).thenReturn(Optional.of(campus));
        when(campusMapper.toResponse(campus)).thenReturn(response);

        assertThat(campusService.getCampusDetails(3L)).isEqualTo(response);
    }

    @Test
    void createCampus_whenNameTaken_throwsConflict() {
        var location = new LocationRequest("FR", "Paris", 2.35, 48.85);
        var request = new CreateCampusRequest("Main", "UTC", location);

        when(organizationRepository.findByCode(OrganizationDefaults.DEFAULT_ORGANIZATION_CODE))
                .thenReturn(Optional.of(defaultOrg));
        when(campusRepository.existsByOrganizationIdAndNameIgnoreCase(1L, "Main")).thenReturn(true);

        assertThatThrownBy(() -> campusService.createCampus(request))
                .isInstanceOf(CampusConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createCampus_savesNewCampus() {
        var location = new LocationRequest("FR", "Lyon", 4.83, 45.75);
        var request = new CreateCampusRequest("Lyon", "Europe/Paris", location);
        Campus mapped = new Campus();
        Location mappedLoc = new Location();
        Campus saved = Campus.builder().id(9L).name("Lyon").active(true).timezone("Europe/Paris").organization(defaultOrg).build();
        CampusResponse response = new CampusResponse(9L, "Lyon", true, "Europe/Paris", 1L, "Org", null);

        when(organizationRepository.findByCode(OrganizationDefaults.DEFAULT_ORGANIZATION_CODE))
                .thenReturn(Optional.of(defaultOrg));
        when(campusRepository.existsByOrganizationIdAndNameIgnoreCase(1L, "Lyon")).thenReturn(false);
        when(campusMapper.toCampus(request)).thenReturn(mapped);
        when(campusMapper.toLocation(location)).thenReturn(mappedLoc);
        when(campusRepository.save(any(Campus.class))).thenReturn(saved);
        when(campusMapper.toResponse(saved)).thenReturn(response);

        assertThat(campusService.createCampus(request)).isEqualTo(response);
        verify(campusRepository).save(any(Campus.class));
    }

    @Test
    void activateCampus_updatesFlag() {
        Campus campus = Campus.builder().id(2L).name("X").active(false).timezone("UTC").organization(defaultOrg).build();
        CampusResponse response = new CampusResponse(2L, "X", true, "UTC", 1L, "Org", null);

        when(organizationRepository.findByCode(OrganizationDefaults.DEFAULT_ORGANIZATION_CODE))
                .thenReturn(Optional.of(defaultOrg));
        when(campusRepository.findByIdAndOrganizationId(2L, 1L)).thenReturn(Optional.of(campus));
        when(campusRepository.save(campus)).thenReturn(campus);
        when(campusMapper.toResponse(campus)).thenReturn(response);

        assertThat(campusService.activateCampus(2L).active()).isTrue();
    }
}

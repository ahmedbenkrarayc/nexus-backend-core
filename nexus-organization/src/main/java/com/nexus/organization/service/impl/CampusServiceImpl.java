package com.nexus.organization.service.impl;

import com.nexus.organization.config.OrganizationDefaults;
import com.nexus.organization.dto.request.campus.CreateCampusRequest;
import com.nexus.organization.dto.request.campus.UpdateCampusRequest;
import com.nexus.organization.dto.response.campus.CampusResponse;
import com.nexus.organization.exception.CampusConflictException;
import com.nexus.organization.exception.ResourceNotFoundException;
import com.nexus.organization.mapper.CampusMapper;
import com.nexus.organization.model.Campus;
import com.nexus.organization.model.Location;
import com.nexus.organization.model.Organization;
import com.nexus.organization.repository.CampusRepository;
import com.nexus.organization.repository.OrganizationRepository;
import com.nexus.organization.service.CampusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampusServiceImpl implements CampusService {

    private final CampusRepository campusRepository;
    private final OrganizationRepository organizationRepository;
    private final CampusMapper campusMapper;

    @Override
    @Transactional
    public CampusResponse createCampus(CreateCampusRequest request) {
        Organization organization = getDefaultOrganization();
        validateCampusName(request.name(), organization.getId(), null);

        Campus campus = campusMapper.toCampus(request);
        campus.setActive(true);
        campus.setOrganization(organization);
        campus.setLocation(campusMapper.toLocation(request.location()));

        return campusMapper.toResponse(campusRepository.save(campus));
    }

    @Override
    @Transactional
    public CampusResponse updateCampus(Long campusId, UpdateCampusRequest request) {
        Organization organization = getDefaultOrganization();
        Campus campus = getCampus(campusId, organization.getId());
        validateCampusName(request.name(), organization.getId(), campusId);

        campusMapper.updateCampus(request, campus);

        if (campus.getLocation() == null) {
            campus.setLocation(campusMapper.toLocation(request.location()));
        } else {
            campusMapper.updateLocation(request.location(), campus.getLocation());
        }

        return campusMapper.toResponse(campusRepository.save(campus));
    }

    @Override
    @Transactional
    public CampusResponse activateCampus(Long campusId) {
        return updateActivation(campusId, true);
    }

    @Override
    @Transactional
    public CampusResponse deactivateCampus(Long campusId) {
        return updateActivation(campusId, false);
    }

    @Override
    public List<CampusResponse> listCampuses() {
        Long organizationId = getDefaultOrganization().getId();
        return campusRepository.findAllByOrganizationIdOrderByIdAsc(organizationId)
                .stream()
                .map(campusMapper::toResponse)
                .toList();
    }

    @Override
    public CampusResponse getCampusDetails(Long campusId) {
        Long organizationId = getDefaultOrganization().getId();
        return campusMapper.toResponse(getCampus(campusId, organizationId));
    }

    private CampusResponse updateActivation(Long campusId, boolean active) {
        Long organizationId = getDefaultOrganization().getId();
        Campus campus = getCampus(campusId, organizationId);
        campus.setActive(active);
        return campusMapper.toResponse(campusRepository.save(campus));
    }

    private Campus getCampus(Long campusId, Long organizationId) {
        return campusRepository.findByIdAndOrganizationId(campusId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Campus not found with id " + campusId));
    }

    private Organization getDefaultOrganization() {
        return organizationRepository.findByCode(OrganizationDefaults.DEFAULT_ORGANIZATION_CODE)
                .orElseThrow(() -> new ResourceNotFoundException("Default organization is not available"));
    }

    private void validateCampusName(String campusName, Long organizationId, Long campusId) {
        boolean alreadyExists = campusId == null
                ? campusRepository.existsByOrganizationIdAndNameIgnoreCase(organizationId, campusName)
                : campusRepository.existsByOrganizationIdAndNameIgnoreCaseAndIdNot(organizationId, campusName, campusId);

        if (alreadyExists) {
            throw new CampusConflictException("Campus name already exists for the default organization");
        }
    }
}
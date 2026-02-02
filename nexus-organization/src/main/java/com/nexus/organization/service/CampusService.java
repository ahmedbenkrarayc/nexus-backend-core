package com.nexus.organization.service;

import com.nexus.organization.dto.request.campus.CreateCampusRequest;
import com.nexus.organization.dto.request.campus.UpdateCampusRequest;
import com.nexus.organization.dto.response.campus.CampusResponse;

import java.util.List;

public interface CampusService {

    CampusResponse createCampus(CreateCampusRequest request);

    CampusResponse updateCampus(Long campusId, UpdateCampusRequest request);

    CampusResponse activateCampus(Long campusId);

    CampusResponse deactivateCampus(Long campusId);

    List<CampusResponse> listCampuses();

    CampusResponse getCampusDetails(Long campusId);
}
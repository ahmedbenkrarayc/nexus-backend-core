package com.nexus.organization.controller;

import com.nexus.organization.dto.request.campus.CreateCampusRequest;
import com.nexus.organization.dto.request.campus.UpdateCampusRequest;
import com.nexus.organization.dto.response.campus.CampusResponse;
import com.nexus.organization.service.CampusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organizations/campuses")
public class CampusController {

    private final CampusService campusService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CampusResponse createCampus(@Valid @RequestBody CreateCampusRequest request) {
        return campusService.createCampus(request);
    }

    @PutMapping("/{campusId}")
    public CampusResponse updateCampus(@PathVariable Long campusId, @Valid @RequestBody UpdateCampusRequest request) {
        return campusService.updateCampus(campusId, request);
    }

    @PostMapping("/{campusId}/activate")
    public CampusResponse activateCampus(@PathVariable Long campusId) {
        return campusService.activateCampus(campusId);
    }

    @PostMapping("/{campusId}/deactivate")
    public CampusResponse deactivateCampus(@PathVariable Long campusId) {
        return campusService.deactivateCampus(campusId);
    }

    @GetMapping
    public List<CampusResponse> listCampuses() {
        return campusService.listCampuses();
    }

    @GetMapping("/{campusId}")
    public CampusResponse getCampusDetails(@PathVariable Long campusId) {
        return campusService.getCampusDetails(campusId);
    }
}
package com.nexus.staffing.controller;

import com.nexus.staffing.dto.request.allocationrequest.CreateAllocationRequest;
import com.nexus.staffing.dto.request.candidatesearch.SearchCandidatesRequest;
import com.nexus.staffing.dto.response.allocationrequest.AllocationRequestResponse;
import com.nexus.staffing.dto.response.candidatesearch.CandidateMatchResponse;
import com.nexus.staffing.dto.response.engagement.ProjectTeamMemberResponse;
import com.nexus.staffing.service.CandidateMatchingService;
import com.nexus.staffing.service.ProjectManagerStaffingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/staffing")
public class ProjectManagerStaffingController {

    private final ProjectManagerStaffingService projectManagerStaffingService;
    private final CandidateMatchingService candidateMatchingService;

    @GetMapping("/projects/{projectId}/team")
    public List<ProjectTeamMemberResponse> listProjectTeam(@PathVariable Long projectId) {
        return projectManagerStaffingService.listProjectTeam(projectId);
    }

    @PostMapping("/allocation-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public AllocationRequestResponse createAllocationRequest(@Valid @RequestBody CreateAllocationRequest request) {
        return projectManagerStaffingService.createAllocationRequest(request);
    }

    @GetMapping("/allocation-requests")
    public List<AllocationRequestResponse> listAllocationRequests(
            @RequestParam(required = false) Long projectId) {
        return projectManagerStaffingService.listAllocationRequests(projectId);
    }

    @PostMapping("/projects/{projectId}/candidate-search")
    public List<CandidateMatchResponse> searchCandidates(
            @PathVariable Long projectId,
            @Valid @RequestBody SearchCandidatesRequest request) {
        // Override projectId in request with path variable
        SearchCandidatesRequest finalRequest = new SearchCandidatesRequest(
            projectId,
            request.requiredSkills(),
            request.niceToHaveSkills(),
            request.minimumSkillLevels(),
            request.engagementLevel(),
            request.requestedStartDate(),
            request.requestedEndDate(),
            request.maxResults()
        );
        return candidateMatchingService.searchCandidates(finalRequest);
    }
}

package com.nexus.staffing.service;

import com.nexus.staffing.dto.request.candidatesearch.SearchCandidatesRequest;
import com.nexus.staffing.dto.response.candidatesearch.CandidateMatchResponse;

import java.util.List;

/**
 * Service for finding and ranking candidate employees for a project based on skills,
 * location, availability, and other factors.
 */
public interface CandidateMatchingService {

    /**
     * Searches for candidate employees matching the given criteria.
     * Returns ranked candidates sorted by match score (highest first), then availability,
     * then by location priority.
     * 
     * @param request Search criteria
     * @return List of matching candidates, sorted by relevance
     * @throws IllegalArgumentException if project not found or PM doesn't own it
     * @throws IllegalArgumentException if date range is invalid
     */
    List<CandidateMatchResponse> searchCandidates(SearchCandidatesRequest request);
}

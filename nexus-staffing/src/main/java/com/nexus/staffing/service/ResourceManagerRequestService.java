package com.nexus.staffing.service;

import com.nexus.staffing.dto.request.resourcemanager.ReviewAllocationRequestDecision;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerAllocationRequestDetailsResponse;
import com.nexus.staffing.dto.response.resourcemanager.ResourceManagerAllocationRequestListItemResponse;

import java.util.List;

public interface ResourceManagerRequestService {

    List<ResourceManagerAllocationRequestListItemResponse> listPendingRequests();

    ResourceManagerAllocationRequestDetailsResponse getRequestDetails(Long requestId);

    ResourceManagerAllocationRequestDetailsResponse approveRequest(Long requestId, ReviewAllocationRequestDecision decision);

    ResourceManagerAllocationRequestDetailsResponse rejectRequest(Long requestId, ReviewAllocationRequestDecision decision);
}

package com.nexus.staffing.repository;

import com.nexus.staffing.model.AllocationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllocationRequestRepository extends JpaRepository<AllocationRequest, Long> {

    List<AllocationRequest> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    List<AllocationRequest> findByProjectIdInOrderByCreatedAtDesc(List<Long> projectIds);

    List<AllocationRequest> findByIdInOrderByCreatedAtDesc(List<Long> ids);
}

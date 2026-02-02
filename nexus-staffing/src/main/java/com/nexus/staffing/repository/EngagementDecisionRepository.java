package com.nexus.staffing.repository;

import com.nexus.staffing.model.EngagementDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EngagementDecisionRepository extends JpaRepository<EngagementDecision, Long> {

    List<EngagementDecision> findByEngagementIdIn(List<Long> engagementIds);
}

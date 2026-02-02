package com.nexus.project.repository;

import com.nexus.project.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    java.util.List<Project> findBySponsorEmployeeId(Long sponsorEmployeeId);

    Page<Project> findBySponsorEmployeeId(Long sponsorEmployeeId, Pageable pageable);

    Optional<Project> findByIdAndSponsorEmployeeId(Long id, Long sponsorEmployeeId);

    boolean existsByNameIgnoreCase(String name);
}

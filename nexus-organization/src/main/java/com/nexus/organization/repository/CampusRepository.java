package com.nexus.organization.repository;

import com.nexus.organization.model.Campus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampusRepository extends JpaRepository<Campus, Long> {

    boolean existsByOrganizationIdAndNameIgnoreCase(Long organizationId, String name);

    boolean existsByOrganizationIdAndNameIgnoreCaseAndIdNot(Long organizationId, String name, Long campusId);

    @EntityGraph(attributePaths = {"organization", "location"})
    List<Campus> findAllByOrganizationIdOrderByIdAsc(Long organizationId);

    @EntityGraph(attributePaths = {"organization", "location"})
    Optional<Campus> findByIdAndOrganizationId(Long campusId, Long organizationId);

    @EntityGraph(attributePaths = {"organization", "location"})
    Optional<Campus> findById(Long campusId);
}
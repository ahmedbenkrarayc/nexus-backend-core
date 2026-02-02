package com.nexus.project.dto.response;

import com.nexus.project.model.enums.ProjectStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjectResponse(
                Long id,
                String name,
                String description,
                LocalDate startDate,
                LocalDate endDate,
                Long ownerCampusId,
                Long sponsorEmployeeId,
                ProjectStatus status,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {
}

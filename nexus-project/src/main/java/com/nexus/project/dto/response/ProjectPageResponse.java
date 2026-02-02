package com.nexus.project.dto.response;

import java.util.List;

public record ProjectPageResponse(
        List<ProjectResponse> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean isFirst,
        boolean isLast
) {
}

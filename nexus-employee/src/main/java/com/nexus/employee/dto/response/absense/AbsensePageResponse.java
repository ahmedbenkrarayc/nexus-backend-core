package com.nexus.employee.dto.response.absense;

import java.util.List;

public record AbsensePageResponse(
    List<AbsenseResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {
}

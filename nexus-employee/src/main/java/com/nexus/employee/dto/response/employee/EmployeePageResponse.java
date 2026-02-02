package com.nexus.employee.dto.response.employee;

import java.util.List;

public record EmployeePageResponse(
        List<EmployeeListItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}

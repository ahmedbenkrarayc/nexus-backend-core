package com.nexus.staffing.service;

import com.nexus.staffing.dto.response.statistics.DashboardStatisticsResponse;

public interface DashboardService {
    DashboardStatisticsResponse getStatistics();
}

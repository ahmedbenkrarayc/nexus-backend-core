package com.nexus.staffing.controller;

import com.nexus.staffing.dto.response.statistics.DashboardStatisticsResponse;
import com.nexus.staffing.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardStatisticsResponse getStatistics() {
        return dashboardService.getStatistics();
    }
}

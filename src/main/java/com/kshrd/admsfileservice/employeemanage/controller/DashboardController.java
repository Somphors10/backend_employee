package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.model.dto.response.ApiResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.DashboardResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.NavigationItemResponse;
import com.kshrd.admsfileservice.employeemanage.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Dashboard", description = "Dashboard totals and sidebar navigation")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard totals")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.of(
                "Dashboard retrieved successfully",
                dashboardService.getDashboard(),
                HttpStatus.OK));
    }

    @GetMapping("/navigation")
    @Operation(summary = "Get sidebar navigation items")
    public ResponseEntity<ApiResponse<List<NavigationItemResponse>>> getNavigation() {
        return ResponseEntity.ok(ApiResponse.of(
                "Navigation retrieved successfully",
                dashboardService.getNavigation(),
                HttpStatus.OK));
    }
}

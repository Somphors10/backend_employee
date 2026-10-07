package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.model.dto.request.PerformanceReviewRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.ApiResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.PerformanceReviewResponse;
import com.kshrd.admsfileservice.employeemanage.service.PerformanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/performance-reviews")
@Tag(name = "Performance", description = "Employee performance reviews")
public class PerformanceController {
    private final PerformanceService performanceService;

    public PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('performance:view')")
    @Operation(summary = "Get performance reviews")
    public ResponseEntity<ApiResponse<List<PerformanceReviewResponse>>> getReviews(
            @RequestParam(required = false) UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.of(
                "Performance reviews retrieved successfully",
                performanceService.getReviews(employeeId),
                HttpStatus.OK));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('performance:view')")
    @Operation(summary = "Get performance review by ID")
    public ResponseEntity<ApiResponse<PerformanceReviewResponse>> getReviewById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.of(
                "Performance review retrieved successfully",
                performanceService.getReviewById(id),
                HttpStatus.OK));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('performance:write')")
    @Operation(summary = "Create a performance review")
    public ResponseEntity<ApiResponse<PerformanceReviewResponse>> createReview(
            @Valid @RequestBody PerformanceReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        "Performance review created successfully",
                        performanceService.createReview(request),
                        HttpStatus.CREATED));
    }
}

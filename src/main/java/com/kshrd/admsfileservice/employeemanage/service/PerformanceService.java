package com.kshrd.admsfileservice.employeemanage.service;

import com.kshrd.admsfileservice.employeemanage.model.dto.request.PerformanceReviewRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.PerformanceReviewResponse;

import java.util.List;
import java.util.UUID;

public interface PerformanceService {
    List<PerformanceReviewResponse> getReviews(UUID employeeId);

    PerformanceReviewResponse getReviewById(UUID id);

    PerformanceReviewResponse createReview(PerformanceReviewRequest request);
}

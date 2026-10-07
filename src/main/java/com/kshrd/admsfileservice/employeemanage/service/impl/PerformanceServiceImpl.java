package com.kshrd.admsfileservice.employeemanage.service.impl;

import com.kshrd.admsfileservice.employeemanage.exception.ResourceNotFoundException;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.PerformanceReviewRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.PerformanceReviewResponse;
import com.kshrd.admsfileservice.employeemanage.model.entity.PerformanceReview;
import com.kshrd.admsfileservice.employeemanage.repository.PerformanceReviewRepository;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import com.kshrd.admsfileservice.employeemanage.service.PerformanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PerformanceServiceImpl implements PerformanceService {
    private final PerformanceReviewRepository performanceReviewRepository;
    private final EmployeeService employeeService;

    public PerformanceServiceImpl(
            PerformanceReviewRepository performanceReviewRepository,
            EmployeeService employeeService) {
        this.performanceReviewRepository = performanceReviewRepository;
        this.employeeService = employeeService;
    }

    @Override
    public List<PerformanceReviewResponse> getReviews(UUID employeeId) {
        List<PerformanceReview> reviews = employeeId == null
                ? performanceReviewRepository.findAll()
                : performanceReviewRepository.findByEmployeeId(employeeId);
        return reviews.stream().map(PerformanceReviewResponse::from).toList();
    }

    @Override
    public PerformanceReviewResponse getReviewById(UUID id) {
        return PerformanceReviewResponse.from(findReview(id));
    }

    @Override
    public PerformanceReviewResponse createReview(PerformanceReviewRequest request) {
        employeeService.getEmployeeById(request.getEmployeeId());
        PerformanceReview review = PerformanceReview.builder()
                .id(UUID.randomUUID())
                .employeeId(request.getEmployeeId())
                .reviewer(request.getReviewer().trim())
                .rating(request.getRating())
                .comments(request.getComments().trim())
                .reviewDate(request.getReviewDate())
                .build();
        return PerformanceReviewResponse.from(performanceReviewRepository.save(review));
    }

    private PerformanceReview findReview(UUID id) {
        return performanceReviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review", id));
    }
}

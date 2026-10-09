package com.kshrd.admsfileservice.employeemanage.service.impl;

import com.kshrd.admsfileservice.employeemanage.exception.ResourceNotFoundException;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.PerformanceReviewRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.PerformanceReviewResponse;
import com.kshrd.admsfileservice.employeemanage.model.entity.PerformanceReview;
import com.kshrd.admsfileservice.employeemanage.repository.PerformanceReviewRepository;
import com.kshrd.admsfileservice.employeemanage.security.AccessService;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import com.kshrd.admsfileservice.employeemanage.service.PerformanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class PerformanceServiceImpl implements PerformanceService {
    private final PerformanceReviewRepository performanceReviewRepository;
    private final EmployeeService employeeService;
    private final AccessService accessService;

    public PerformanceServiceImpl(
            PerformanceReviewRepository performanceReviewRepository,
            EmployeeService employeeService,
            AccessService accessService) {
        this.performanceReviewRepository = performanceReviewRepository;
        this.employeeService = employeeService;
        this.accessService = accessService;
    }

    @Override
    public List<PerformanceReviewResponse> getReviews(UUID employeeId) {
        UUID scoped = accessService.resolveEmployeeId(employeeId);
        Set<UUID> visible = accessService.visibleEmployeeIds();
        List<PerformanceReview> reviews = scoped == null
                ? performanceReviewRepository.findAll()
                : performanceReviewRepository.findByEmployeeId(scoped);
        return reviews.stream()
                .filter(review -> visible == null || visible.contains(review.getEmployeeId()))
                .map(PerformanceReviewResponse::from)
                .toList();
    }

    @Override
    public PerformanceReviewResponse getReviewById(UUID id) {
        PerformanceReview review = findReview(id);
        accessService.assertCanViewEmployee(review.getEmployeeId());
        return PerformanceReviewResponse.from(review);
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

    @Override
    public PerformanceReviewResponse updateReview(UUID id, PerformanceReviewRequest request) {
        PerformanceReview review = findReview(id);
        employeeService.getEmployeeById(request.getEmployeeId());
        review.setEmployeeId(request.getEmployeeId());
        review.setReviewer(request.getReviewer().trim());
        review.setRating(request.getRating());
        review.setComments(request.getComments().trim());
        review.setReviewDate(request.getReviewDate());
        return PerformanceReviewResponse.from(performanceReviewRepository.save(review));
    }

    @Override
    public void deleteReview(UUID id) {
        performanceReviewRepository.delete(findReview(id));
    }

    private PerformanceReview findReview(UUID id) {
        return performanceReviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review", id));
    }
}

package com.kshrd.admsfileservice.employeemanage.model.dto.response;

import com.kshrd.admsfileservice.employeemanage.model.entity.PerformanceReview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceReviewResponse {
    private UUID id;
    private UUID employeeId;
    private String reviewer;
    private int rating;
    private String comments;
    private LocalDate reviewDate;

    public static PerformanceReviewResponse from(PerformanceReview review) {
        return PerformanceReviewResponse.builder()
                .id(review.getId())
                .employeeId(review.getEmployeeId())
                .reviewer(review.getReviewer())
                .rating(review.getRating())
                .comments(review.getComments())
                .reviewDate(review.getReviewDate())
                .build();
    }
}

package com.kshrd.admsfileservice.employeemanage.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class PerformanceReviewRequest {
    @NotNull(message = "Employee id is required")
    private UUID employeeId;

    @NotBlank(message = "Reviewer is required")
    private String reviewer;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating;

    @NotBlank(message = "Comments are required")
    private String comments;

    @NotNull(message = "Review date is required")
    private LocalDate reviewDate;
}

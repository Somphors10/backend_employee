package com.kshrd.admsfileservice.employeemanage.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCheckRequest {
    @NotNull(message = "Employee id is required")
    private UUID employeeId;
}

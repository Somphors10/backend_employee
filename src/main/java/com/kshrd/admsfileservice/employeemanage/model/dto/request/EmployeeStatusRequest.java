package com.kshrd.admsfileservice.employeemanage.model.dto.request;

import com.kshrd.admsfileservice.employeemanage.model.enums.EmploymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeStatusRequest {
    @NotNull(message = "Status is required")
    private EmploymentStatus status;
}

package com.kshrd.admsfileservice.employeemanage.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDepartmentRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private UUID managerId;
}

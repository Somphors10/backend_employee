package com.kshrd.admsfileservice.employeemanage.model.dto.response;

import com.kshrd.admsfileservice.employeemanage.model.entity.OrganizationDepartment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDepartmentResponse {
    private UUID id;
    private String name;
    private String description;
    private UUID managerId;

    public static OrganizationDepartmentResponse from(OrganizationDepartment department) {
        return OrganizationDepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .managerId(department.getManagerId())
                .build();
    }
}

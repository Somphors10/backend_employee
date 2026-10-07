package com.kshrd.admsfileservice.employeemanage.service;

import com.kshrd.admsfileservice.employeemanage.model.dto.request.OrganizationDepartmentRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.OrganizationDepartmentResponse;

import java.util.List;
import java.util.UUID;

public interface OrganizationService {
    List<OrganizationDepartmentResponse> getDepartments();

    OrganizationDepartmentResponse getDepartmentById(UUID id);

    OrganizationDepartmentResponse createDepartment(OrganizationDepartmentRequest request);

    OrganizationDepartmentResponse updateDepartment(UUID id, OrganizationDepartmentRequest request);

    void deleteDepartment(UUID id);
}

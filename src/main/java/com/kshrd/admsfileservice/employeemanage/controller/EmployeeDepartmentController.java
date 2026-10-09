package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.model.dto.request.OrganizationDepartmentRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.ApiResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.OrganizationDepartmentResponse;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import com.kshrd.admsfileservice.employeemanage.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees/departments")
@Tag(name = "Employee Department", description = "Create, update, delete, and list employee departments")
public class EmployeeDepartmentController {
    private final EmployeeService employeeService;
    private final OrganizationService organizationService;

    public EmployeeDepartmentController(EmployeeService employeeService, OrganizationService organizationService) {
        this.employeeService = employeeService;
        this.organizationService = organizationService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('employees:view')")
    @Operation(summary = "Get all department names")
    public ResponseEntity<ApiResponse<List<String>>> getDepartments() {
        return ResponseEntity.ok(ApiResponse.of(
                "Departments retrieved successfully",
                employeeService.getDepartments(),
                HttpStatus.OK));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('organization:view')")
    @Operation(summary = "Get a department by ID")
    public ResponseEntity<ApiResponse<OrganizationDepartmentResponse>> getDepartmentById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.of(
                "Department retrieved successfully",
                organizationService.getDepartmentById(id),
                HttpStatus.OK));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('organization:write')")
    @Operation(summary = "Create a department")
    public ResponseEntity<ApiResponse<OrganizationDepartmentResponse>> createDepartment(
            @Valid @RequestBody OrganizationDepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        "Department created successfully",
                        organizationService.createDepartment(request),
                        HttpStatus.CREATED));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('organization:write')")
    @Operation(summary = "Update a department")
    public ResponseEntity<ApiResponse<OrganizationDepartmentResponse>> updateDepartment(
            @PathVariable UUID id,
            @Valid @RequestBody OrganizationDepartmentRequest request) {
        return ResponseEntity.ok(ApiResponse.of(
                "Department updated successfully",
                organizationService.updateDepartment(id, request),
                HttpStatus.OK));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('organization:write')")
    @Operation(summary = "Delete a department")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable UUID id) {
        organizationService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.of("Department deleted successfully", null, HttpStatus.OK));
    }
}

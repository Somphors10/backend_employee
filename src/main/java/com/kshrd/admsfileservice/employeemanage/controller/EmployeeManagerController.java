package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.model.dto.request.EmployeeManagerRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.ApiResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.EmployeeResponse;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee Manager", description = "Assign managers and list subordinates")
public class EmployeeManagerController {
    private final EmployeeService employeeService;

    public EmployeeManagerController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/{id}/subordinates")
    @PreAuthorize("hasAuthority('employees:view')")
    @Operation(summary = "Get employees who report to this manager")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getSubordinates(@PathVariable UUID id) {
        List<EmployeeResponse> subordinates = employeeService.getSubordinates(id);
        return ResponseEntity.ok(ApiResponse.of("Subordinates retrieved successfully", subordinates, HttpStatus.OK));
    }

    @PatchMapping("/{id}/manager")
    @PreAuthorize("hasAuthority('employees:write')")
    @Operation(summary = "Assign a manager to an employee")
    public ResponseEntity<ApiResponse<EmployeeResponse>> assignManager(
            @PathVariable UUID id,
            @Valid @RequestBody EmployeeManagerRequest request) {
        EmployeeResponse employee = employeeService.assignManager(id, request);
        return ResponseEntity.ok(ApiResponse.of("Manager assigned successfully", employee, HttpStatus.OK));
    }

    @DeleteMapping("/{id}/manager")
    @PreAuthorize("hasAuthority('employees:write')")
    @Operation(summary = "Clear an employee's manager")
    public ResponseEntity<ApiResponse<EmployeeResponse>> clearManager(@PathVariable UUID id) {
        EmployeeResponse employee = employeeService.clearManager(id);
        return ResponseEntity.ok(ApiResponse.of("Manager cleared successfully", employee, HttpStatus.OK));
    }
}

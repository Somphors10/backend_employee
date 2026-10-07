package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.model.dto.response.ApiResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.EmployeeResponse;
import com.kshrd.admsfileservice.employeemanage.model.enums.EmploymentStatus;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees/search")
@Tag(name = "Employee Search", description = "Search and filter employees")
public class EmployeeSearchController {
    private final EmployeeService employeeService;

    public EmployeeSearchController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('employees:view')")
    @Operation(summary = "Search employees", description = "Filter by department, status, or search query")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> searchEmployees(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) EmploymentStatus status) {
        List<EmployeeResponse> employees = employeeService.getAllEmployees(department, q, status);
        return ResponseEntity.ok(ApiResponse.of("Employees retrieved successfully", employees, HttpStatus.OK));
    }
}

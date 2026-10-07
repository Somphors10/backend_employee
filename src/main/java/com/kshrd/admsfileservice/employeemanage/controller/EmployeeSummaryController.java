package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.model.dto.response.ApiResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.EmployeeSummaryResponse;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employees/summary")
@Tag(name = "Employee Summary", description = "Employee totals and department counts")
public class EmployeeSummaryController {
    private final EmployeeService employeeService;

    public EmployeeSummaryController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('employees:view')")
    @Operation(summary = "Get employee summary", description = "Totals and counts grouped by department")
    public ResponseEntity<ApiResponse<EmployeeSummaryResponse>> getEmployeeSummary() {
        EmployeeSummaryResponse summary = employeeService.getEmployeeSummary();
        return ResponseEntity.ok(ApiResponse.of("Employee summary retrieved successfully", summary, HttpStatus.OK));
    }
}

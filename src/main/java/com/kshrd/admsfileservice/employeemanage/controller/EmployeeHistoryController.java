package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.model.dto.response.ApiResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.EmployeeHistoryResponse;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee History", description = "Employment activity history")
public class EmployeeHistoryController {
    private final EmployeeService employeeService;

    public EmployeeHistoryController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('employees:view')")
    @Operation(summary = "Get an employee's activity history")
    public ResponseEntity<ApiResponse<List<EmployeeHistoryResponse>>> getEmployeeHistory(@PathVariable UUID id) {
        List<EmployeeHistoryResponse> history = employeeService.getEmployeeHistory(id);
        return ResponseEntity.ok(ApiResponse.of("Employee history retrieved successfully", history, HttpStatus.OK));
    }
}

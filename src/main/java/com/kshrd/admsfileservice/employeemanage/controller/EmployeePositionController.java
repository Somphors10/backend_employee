package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.model.dto.response.ApiResponse;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees/positions")
@Tag(name = "Employee Position", description = "List employee positions")
public class EmployeePositionController {
    private final EmployeeService employeeService;

    public EmployeePositionController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('employees:view')")
    @Operation(summary = "Get all positions")
    public ResponseEntity<ApiResponse<List<String>>> getPositions() {
        List<String> positions = employeeService.getPositions();
        return ResponseEntity.ok(ApiResponse.of("Positions retrieved successfully", positions, HttpStatus.OK));
    }
}

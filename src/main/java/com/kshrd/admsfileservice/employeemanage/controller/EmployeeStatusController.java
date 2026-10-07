package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.model.dto.request.EmployeeStatusRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.ApiResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.EmployeeResponse;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee Status", description = "Activate or deactivate employees")
public class EmployeeStatusController {
    private final EmployeeService employeeService;

    public EmployeeStatusController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activate or deactivate an employee")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployeeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody EmployeeStatusRequest request) {
        EmployeeResponse employee = employeeService.updateEmployeeStatus(id, request);
        return ResponseEntity.ok(ApiResponse.of("Employee status updated successfully", employee, HttpStatus.OK));
    }
}

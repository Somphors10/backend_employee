package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.model.dto.request.EmployeeTransferRequest;
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
@Tag(name = "Employee Transfer", description = "Transfer employees to another department or position")
public class EmployeeTransferController {
    private final EmployeeService employeeService;

    public EmployeeTransferController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PatchMapping("/{id}/transfer")
    @Operation(summary = "Transfer an employee to another department or position")
    public ResponseEntity<ApiResponse<EmployeeResponse>> transferEmployee(
            @PathVariable UUID id,
            @Valid @RequestBody EmployeeTransferRequest request) {
        EmployeeResponse employee = employeeService.transferEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.of("Employee transferred successfully", employee, HttpStatus.OK));
    }
}

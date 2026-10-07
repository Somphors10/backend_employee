package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.model.dto.response.ApiResponse;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees/departments")
@Tag(name = "Employee Department", description = "List employee departments")
public class EmployeeDepartmentController {
    private final EmployeeService employeeService;

    public EmployeeDepartmentController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @Operation(summary = "Get all departments")
    public ResponseEntity<ApiResponse<List<String>>> getDepartments() {
        List<String> departments = employeeService.getDepartments();
        return ResponseEntity.ok(ApiResponse.of("Departments retrieved successfully", departments, HttpStatus.OK));
    }
}

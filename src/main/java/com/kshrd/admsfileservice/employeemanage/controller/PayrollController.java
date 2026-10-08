package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.model.dto.request.PayrollRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.ApiResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.PayrollResponse;
import com.kshrd.admsfileservice.employeemanage.model.enums.PayrollStatus;
import com.kshrd.admsfileservice.employeemanage.service.PayrollService;
import com.kshrd.admsfileservice.employeemanage.service.StoredDocument;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payrolls")
@Tag(name = "Payroll", description = "Employee payroll records")
public class PayrollController {
    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('payroll:view')")
    @Operation(summary = "Get payroll records")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getPayrolls(
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) PayrollStatus status) {
        return ResponseEntity.ok(ApiResponse.of(
                "Payroll records retrieved successfully",
                payrollService.getPayrolls(employeeId, status),
                HttpStatus.OK));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('payroll:view')")
    @Operation(summary = "Get payroll by ID")
    public ResponseEntity<ApiResponse<PayrollResponse>> getPayrollById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.of(
                "Payroll retrieved successfully",
                payrollService.getPayrollById(id),
                HttpStatus.OK));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('payroll:write')")
    @Operation(summary = "Create a payroll record")
    public ResponseEntity<ApiResponse<PayrollResponse>> createPayroll(@Valid @RequestBody PayrollRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("Payroll created successfully", payrollService.createPayroll(request), HttpStatus.CREATED));
    }

    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasAuthority('payroll:write')")
    @Operation(summary = "Mark a payroll record as paid")
    public ResponseEntity<ApiResponse<PayrollResponse>> markPaid(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.of("Payroll marked as paid", payrollService.markPaid(id), HttpStatus.OK));
    }

    @GetMapping("/{id}/payslip")
    @PreAuthorize("hasAuthority('payroll:view')")
    @Operation(summary = "Download payslip PDF")
    public ResponseEntity<Resource> downloadPayslip(@PathVariable UUID id) {
        StoredDocument file = payrollService.getPayslip(id);
        ContentDisposition disposition = ContentDisposition.inline()
                .filename(file.originalFileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentLength(file.fileSize())
                .body(file.resource());
    }
}

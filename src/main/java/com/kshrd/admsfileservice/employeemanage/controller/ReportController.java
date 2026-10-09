package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.model.dto.response.ApiResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.ReportOverviewResponse;
import com.kshrd.admsfileservice.employeemanage.service.ReportService;
import com.kshrd.admsfileservice.employeemanage.service.StoredDocument;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "HR reports: headcount, leave, attendance, payroll, overtime, performance, CSV export")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('reports:view')")
    @Operation(summary = "HR report overview", description = "Headcount plus leave, attendance, payroll, overtime, and performance for a date range")
    public ResponseEntity<ApiResponse<ReportOverviewResponse>> summary(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.of(
                "Report summary retrieved successfully",
                reportService.getOverview(from, to),
                HttpStatus.OK));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('reports:view')")
    @Operation(summary = "Download a CSV report", description = "type=people|leaves|attendance|payrolls|overtimes|performance")
    public ResponseEntity<Resource> export(
            @RequestParam String type,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        StoredDocument file = reportService.exportCsv(type, from, to);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.originalFileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentLength(file.fileSize())
                .body(file.resource());
    }
}

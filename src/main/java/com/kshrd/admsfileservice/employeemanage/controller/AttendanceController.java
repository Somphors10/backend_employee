package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.model.dto.request.AttendanceCheckRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.ApiResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.AttendanceResponse;
import com.kshrd.admsfileservice.employeemanage.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attendances")
@Tag(name = "Attendance", description = "Employee check-in and check-out")
public class AttendanceController {
    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    @Operation(summary = "Get attendance records")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendances(
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.of(
                "Attendance records retrieved successfully",
                attendanceService.getAttendances(employeeId, date),
                HttpStatus.OK));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get attendance by ID")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getAttendanceById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.of(
                "Attendance retrieved successfully",
                attendanceService.getAttendanceById(id),
                HttpStatus.OK));
    }

    @PostMapping("/check-in")
    @Operation(summary = "Check in an employee")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn(
            @Valid @RequestBody AttendanceCheckRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("Checked in successfully", attendanceService.checkIn(request), HttpStatus.CREATED));
    }

    @PostMapping("/check-out")
    @Operation(summary = "Check out an employee")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkOut(
            @Valid @RequestBody AttendanceCheckRequest request) {
        return ResponseEntity.ok(ApiResponse.of(
                "Checked out successfully",
                attendanceService.checkOut(request),
                HttpStatus.OK));
    }
}

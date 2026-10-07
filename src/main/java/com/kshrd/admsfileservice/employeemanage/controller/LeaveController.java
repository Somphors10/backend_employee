package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.model.dto.request.LeaveRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.ApiResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.LeaveResponse;
import com.kshrd.admsfileservice.employeemanage.model.enums.LeaveStatus;
import com.kshrd.admsfileservice.employeemanage.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leaves")
@Tag(name = "Employee Leave", description = "Apply, review, and decide employee leave requests")
public class LeaveController {
    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping
    @Operation(summary = "Get leave requests", description = "Optionally filter by employee and status")
    public ResponseEntity<ApiResponse<List<LeaveResponse>>> getLeaves(
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) LeaveStatus status) {
        List<LeaveResponse> leaves = leaveService.getLeaves(employeeId, status);
        return ResponseEntity.ok(ApiResponse.of("Leave requests retrieved successfully", leaves, HttpStatus.OK));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a leave request by ID")
    public ResponseEntity<ApiResponse<LeaveResponse>> getLeaveById(@PathVariable UUID id) {
        LeaveResponse leave = leaveService.getLeaveById(id);
        return ResponseEntity.ok(ApiResponse.of("Leave request retrieved successfully", leave, HttpStatus.OK));
    }

    @PostMapping
    @Operation(summary = "Submit a leave request")
    public ResponseEntity<ApiResponse<LeaveResponse>> createLeave(@Valid @RequestBody LeaveRequest request) {
        LeaveResponse leave = leaveService.createLeave(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("Leave request created successfully", leave, HttpStatus.CREATED));
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Approve a pending leave request")
    public ResponseEntity<ApiResponse<LeaveResponse>> approveLeave(@PathVariable UUID id) {
        LeaveResponse leave = leaveService.approveLeave(id);
        return ResponseEntity.ok(ApiResponse.of("Leave request approved successfully", leave, HttpStatus.OK));
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Reject a pending leave request")
    public ResponseEntity<ApiResponse<LeaveResponse>> rejectLeave(@PathVariable UUID id) {
        LeaveResponse leave = leaveService.rejectLeave(id);
        return ResponseEntity.ok(ApiResponse.of("Leave request rejected successfully", leave, HttpStatus.OK));
    }
}

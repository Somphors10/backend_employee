package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.exception.InvalidOperationException;
import com.kshrd.admsfileservice.employeemanage.exception.ResourceNotFoundException;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.ApiResponse;
import com.kshrd.admsfileservice.employeemanage.model.entity.OvertimeRequest;
import com.kshrd.admsfileservice.employeemanage.model.enums.RequestStatus;
import com.kshrd.admsfileservice.employeemanage.repository.OvertimeRequestRepository;
import com.kshrd.admsfileservice.employeemanage.security.AccessService;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import com.kshrd.admsfileservice.employeemanage.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/overtimes")
@Tag(name = "Overtime", description = "Overtime requests")
public class OvertimeController {
    private final OvertimeRequestRepository overtimeRepository;
    private final EmployeeService employeeService;
    private final AccessService accessService;
    private final NotificationService notificationService;

    public OvertimeController(
            OvertimeRequestRepository overtimeRepository,
            EmployeeService employeeService,
            AccessService accessService,
            NotificationService notificationService) {
        this.overtimeRepository = overtimeRepository;
        this.employeeService = employeeService;
        this.accessService = accessService;
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('overtime:view')")
    @Operation(summary = "List overtime requests")
    public ResponseEntity<ApiResponse<List<OvertimeRequest>>> getOvertimes(
            @RequestParam(required = false) UUID employeeId) {
        UUID scoped = accessService.resolveEmployeeId(employeeId);
        Set<UUID> visible = accessService.visibleEmployeeIds();
        List<OvertimeRequest> items = overtimeRepository.findAll().stream()
                .filter(item -> scoped == null || item.getEmployeeId().equals(scoped))
                .filter(item -> visible == null || visible.contains(item.getEmployeeId()))
                .sorted(Comparator.comparing(OvertimeRequest::getWorkDate).reversed())
                .toList();
        return ResponseEntity.ok(ApiResponse.of("Overtime requests retrieved successfully", items, HttpStatus.OK));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('overtime:write')")
    @Operation(summary = "Submit an overtime request")
    public ResponseEntity<ApiResponse<OvertimeRequest>> create(@RequestBody OvertimeBody body) {
        if (body.employeeId() == null || body.workDate() == null || body.hours() == null || body.reason() == null) {
            throw new InvalidOperationException("Employee, date, hours, and reason are required");
        }
        accessService.assertCanActAsEmployee(body.employeeId());
        employeeService.getEmployeeById(body.employeeId());
        if (body.hours().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Hours must be greater than 0");
        }
        OvertimeRequest saved = overtimeRepository.save(OvertimeRequest.builder()
                .id(UUID.randomUUID())
                .employeeId(body.employeeId())
                .workDate(body.workDate())
                .hours(body.hours())
                .reason(body.reason().trim())
                .status(RequestStatus.PENDING)
                .build());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("Overtime request submitted", saved, HttpStatus.CREATED));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('overtime:decide')")
    @Operation(summary = "Approve overtime")
    public ResponseEntity<ApiResponse<OvertimeRequest>> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.of("Overtime approved", decide(id, RequestStatus.APPROVED), HttpStatus.OK));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('overtime:decide')")
    @Operation(summary = "Reject overtime")
    public ResponseEntity<ApiResponse<OvertimeRequest>> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.of("Overtime rejected", decide(id, RequestStatus.REJECTED), HttpStatus.OK));
    }

    private OvertimeRequest decide(UUID id, RequestStatus status) {
        OvertimeRequest overtime = overtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Overtime", id));
        accessService.assertCanViewEmployee(overtime.getEmployeeId());
        if (overtime.getStatus() != RequestStatus.PENDING) {
            throw new InvalidOperationException("Only pending overtime can be decided");
        }
        overtime.setStatus(status);
        overtime.setDecidedAt(Instant.now());
        OvertimeRequest saved = overtimeRepository.save(overtime);
        notificationService.notifyEmployee(
                saved.getEmployeeId(),
                "Overtime " + status.name().toLowerCase(),
                "Your overtime on " + saved.getWorkDate() + " was " + status.name().toLowerCase() + ".");
        return saved;
    }

    public record OvertimeBody(UUID employeeId, LocalDate workDate, BigDecimal hours, String reason) {
    }
}

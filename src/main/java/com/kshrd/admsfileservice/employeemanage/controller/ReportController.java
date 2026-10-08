package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.model.dto.response.ApiResponse;
import com.kshrd.admsfileservice.employeemanage.model.enums.LeaveStatus;
import com.kshrd.admsfileservice.employeemanage.model.enums.PayrollStatus;
import com.kshrd.admsfileservice.employeemanage.model.enums.RequestStatus;
import com.kshrd.admsfileservice.employeemanage.repository.AttendanceRepository;
import com.kshrd.admsfileservice.employeemanage.repository.EmployeeRepository;
import com.kshrd.admsfileservice.employeemanage.repository.LeaveRepository;
import com.kshrd.admsfileservice.employeemanage.repository.OvertimeRequestRepository;
import com.kshrd.admsfileservice.employeemanage.repository.PayrollRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "HR summary reports")
public class ReportController {
    private final EmployeeRepository employeeRepository;
    private final LeaveRepository leaveRepository;
    private final AttendanceRepository attendanceRepository;
    private final PayrollRepository payrollRepository;
    private final OvertimeRequestRepository overtimeRequestRepository;

    public ReportController(
            EmployeeRepository employeeRepository,
            LeaveRepository leaveRepository,
            AttendanceRepository attendanceRepository,
            PayrollRepository payrollRepository,
            OvertimeRequestRepository overtimeRequestRepository) {
        this.employeeRepository = employeeRepository;
        this.leaveRepository = leaveRepository;
        this.attendanceRepository = attendanceRepository;
        this.payrollRepository = payrollRepository;
        this.overtimeRequestRepository = overtimeRequestRepository;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('reports:view')")
    @Operation(summary = "HR summary counts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> summary() {
        Map<String, Object> payload = Map.of(
                "employees", employeeRepository.count(),
                "pendingLeaves", leaveRepository.countByStatus(LeaveStatus.PENDING),
                "todayAttendance", attendanceRepository.countByWorkDate(LocalDate.now()),
                "pendingPayrolls", payrollRepository.countByStatus(PayrollStatus.PENDING),
                "pendingOvertimes", overtimeRequestRepository.findAll().stream()
                        .filter(item -> item.getStatus() == RequestStatus.PENDING)
                        .count()
        );
        return ResponseEntity.ok(ApiResponse.of("Report summary retrieved successfully", payload, HttpStatus.OK));
    }
}

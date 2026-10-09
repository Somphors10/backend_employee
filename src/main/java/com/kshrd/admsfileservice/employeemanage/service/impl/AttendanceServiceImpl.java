package com.kshrd.admsfileservice.employeemanage.service.impl;

import com.kshrd.admsfileservice.employeemanage.exception.InvalidOperationException;
import com.kshrd.admsfileservice.employeemanage.exception.ResourceNotFoundException;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.AttendanceCheckRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.AttendanceCorrectionRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.AttendanceResponse;
import com.kshrd.admsfileservice.employeemanage.model.entity.Attendance;
import com.kshrd.admsfileservice.employeemanage.model.enums.AttendanceStatus;
import com.kshrd.admsfileservice.employeemanage.repository.AttendanceRepository;
import com.kshrd.admsfileservice.employeemanage.security.AccessService;
import com.kshrd.admsfileservice.employeemanage.service.AttendanceService;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final EmployeeService employeeService;
    private final AccessService accessService;

    public AttendanceServiceImpl(
            AttendanceRepository attendanceRepository,
            EmployeeService employeeService,
            AccessService accessService) {
        this.attendanceRepository = attendanceRepository;
        this.employeeService = employeeService;
        this.accessService = accessService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendances(UUID employeeId, LocalDate date, LocalDate from, LocalDate to) {
        UUID scoped = accessService.resolveEmployeeId(employeeId);
        Set<UUID> visible = accessService.visibleEmployeeIds();
        LocalDate start = from;
        LocalDate end = to;
        if (date != null) {
            start = date;
            end = date;
        }
        LocalDate rangeStart = start;
        LocalDate rangeEnd = end;
        return attendanceRepository.findAll().stream()
                .filter(item -> scoped == null || item.getEmployeeId().equals(scoped))
                .filter(item -> visible == null || visible.contains(item.getEmployeeId()))
                .filter(item -> rangeStart == null || !item.getWorkDate().isBefore(rangeStart))
                .filter(item -> rangeEnd == null || !item.getWorkDate().isAfter(rangeEnd))
                .map(AttendanceResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getAttendanceById(UUID id) {
        Attendance attendance = findAttendance(id);
        accessService.assertCanViewEmployee(attendance.getEmployeeId());
        return AttendanceResponse.from(attendance);
    }

    @Override
    public AttendanceResponse checkIn(AttendanceCheckRequest request) {
        accessService.assertCanActAsEmployee(request.getEmployeeId());
        employeeService.getEmployeeById(request.getEmployeeId());
        LocalDate today = LocalDate.now();
        attendanceRepository.findByEmployeeIdAndWorkDate(request.getEmployeeId(), today)
                .ifPresent(existing -> {
                    throw new InvalidOperationException("Employee already checked in today");
                });
        LocalTime now = LocalTime.now();
        Attendance attendance = Attendance.builder()
                .id(UUID.randomUUID())
                .employeeId(request.getEmployeeId())
                .workDate(today)
                .checkIn(now)
                .status(now.isAfter(LocalTime.of(9, 0)) ? AttendanceStatus.LATE : AttendanceStatus.PRESENT)
                .build();
        return AttendanceResponse.from(attendanceRepository.save(attendance));
    }

    @Override
    public AttendanceResponse checkOut(AttendanceCheckRequest request) {
        accessService.assertCanActAsEmployee(request.getEmployeeId());
        employeeService.getEmployeeById(request.getEmployeeId());
        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndWorkDate(request.getEmployeeId(), LocalDate.now())
                .orElseThrow(() -> new InvalidOperationException("Employee has not checked in today"));
        if (attendance.getCheckOut() != null) {
            throw new InvalidOperationException("Employee already checked out today");
        }
        attendance.setCheckOut(LocalTime.now());
        return AttendanceResponse.from(attendanceRepository.save(attendance));
    }

    @Override
    public AttendanceResponse correct(UUID id, AttendanceCorrectionRequest request) {
        Attendance attendance = findAttendance(id);
        accessService.assertCanCorrectAttendance(attendance.getEmployeeId());
        if (request.getCheckIn() != null) {
            attendance.setCheckIn(request.getCheckIn());
        }
        if (request.getCheckOut() != null) {
            attendance.setCheckOut(request.getCheckOut());
        }
        if (request.getStatus() != null) {
            attendance.setStatus(request.getStatus());
        }
        if (request.getOvertimeHours() != null) {
            attendance.setOvertimeHours(request.getOvertimeHours());
        }
        return AttendanceResponse.from(attendanceRepository.save(attendance));
    }

    private Attendance findAttendance(UUID id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", id));
    }
}

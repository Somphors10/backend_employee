package com.kshrd.admsfileservice.employeemanage.service.impl;

import com.kshrd.admsfileservice.employeemanage.exception.InvalidOperationException;
import com.kshrd.admsfileservice.employeemanage.exception.ResourceNotFoundException;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.AttendanceCheckRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.AttendanceResponse;
import com.kshrd.admsfileservice.employeemanage.model.entity.Attendance;
import com.kshrd.admsfileservice.employeemanage.model.enums.AttendanceStatus;
import com.kshrd.admsfileservice.employeemanage.repository.AttendanceRepository;
import com.kshrd.admsfileservice.employeemanage.service.AttendanceService;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final EmployeeService employeeService;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository, EmployeeService employeeService) {
        this.attendanceRepository = attendanceRepository;
        this.employeeService = employeeService;
    }

    @Override
    public List<AttendanceResponse> getAttendances(UUID employeeId, LocalDate date) {
        if (employeeId != null && date != null) {
            return attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, date).stream()
                    .map(AttendanceResponse::from)
                    .toList();
        }
        if (employeeId != null) {
            return attendanceRepository.findByEmployeeId(employeeId).stream()
                    .map(AttendanceResponse::from)
                    .toList();
        }
        if (date != null) {
            return attendanceRepository.findByWorkDate(date).stream()
                    .map(AttendanceResponse::from)
                    .toList();
        }
        return attendanceRepository.findAll().stream()
                .map(AttendanceResponse::from)
                .toList();
    }

    @Override
    public AttendanceResponse getAttendanceById(UUID id) {
        return AttendanceResponse.from(findAttendance(id));
    }

    @Override
    public AttendanceResponse checkIn(AttendanceCheckRequest request) {
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

    private Attendance findAttendance(UUID id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", id));
    }
}

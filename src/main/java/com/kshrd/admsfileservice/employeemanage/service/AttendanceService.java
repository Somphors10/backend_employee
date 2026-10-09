package com.kshrd.admsfileservice.employeemanage.service;

import com.kshrd.admsfileservice.employeemanage.model.dto.request.AttendanceCheckRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.AttendanceCorrectionRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.AttendanceResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AttendanceService {
    List<AttendanceResponse> getAttendances(UUID employeeId, LocalDate date, LocalDate from, LocalDate to);

    AttendanceResponse getAttendanceById(UUID id);

    AttendanceResponse checkIn(AttendanceCheckRequest request);

    AttendanceResponse checkOut(AttendanceCheckRequest request);

    AttendanceResponse correct(UUID id, AttendanceCorrectionRequest request);
}

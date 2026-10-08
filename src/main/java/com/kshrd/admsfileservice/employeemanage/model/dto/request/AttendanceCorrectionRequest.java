package com.kshrd.admsfileservice.employeemanage.model.dto.request;

import com.kshrd.admsfileservice.employeemanage.model.enums.AttendanceStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class AttendanceCorrectionRequest {
    private LocalTime checkIn;
    private LocalTime checkOut;
    private AttendanceStatus status;
    private BigDecimal overtimeHours;
}

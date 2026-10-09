package com.kshrd.admsfileservice.employeemanage.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportOverviewResponse {
    private LocalDate from;
    private LocalDate to;
    private Instant generatedAt;

    private long employees;
    private long pendingLeaves;
    private long todayAttendance;
    private long pendingPayrolls;
    private long pendingOvertimes;

    private PeopleReport people;
    private LeaveReport leaves;
    private AttendanceReport attendance;
    private PayrollReport payroll;
    private OvertimeReport overtime;
    private PerformanceReport performance;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeopleReport {
        private long total;
        private long active;
        private long inactive;
        private long newHires;
        private List<NamedCountResponse> byDepartment;
        private List<NamedCountResponse> byStatus;
        private List<NamedCountResponse> byPosition;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaveReport {
        private long total;
        private long pending;
        private long approved;
        private long rejected;
        private long cancelled;
        private long approvedDays;
        private List<NamedCountResponse> byType;
        private List<NamedCountResponse> byStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceReport {
        private long records;
        private long present;
        private long absent;
        private long late;
        private long onLeave;
        private List<NamedCountResponse> byStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayrollReport {
        private long records;
        private long pending;
        private long paid;
        private BigDecimal pendingAmount;
        private BigDecimal paidAmount;
        private BigDecimal totalAmount;
        private List<NamedCountResponse> byStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OvertimeReport {
        private long records;
        private long pending;
        private long approved;
        private long rejected;
        private BigDecimal pendingHours;
        private BigDecimal approvedHours;
        private List<NamedCountResponse> byStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceReport {
        private long reviews;
        private double averageRating;
        private List<NamedCountResponse> byRating;
    }
}

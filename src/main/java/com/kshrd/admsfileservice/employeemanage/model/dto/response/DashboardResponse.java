package com.kshrd.admsfileservice.employeemanage.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private long totalEmployees;
    private long activeEmployees;
    private long pendingLeaves;
    private long todayAttendance;
    private long pendingPayrolls;
}

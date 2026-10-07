package com.kshrd.admsfileservice.employeemanage.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSummaryResponse {
    private long totalEmployees;
    private long activeEmployees;
    private long inactiveEmployees;
    private List<DepartmentSummary> byDepartment;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentSummary {
        private String department;
        private long employeeCount;
    }
}

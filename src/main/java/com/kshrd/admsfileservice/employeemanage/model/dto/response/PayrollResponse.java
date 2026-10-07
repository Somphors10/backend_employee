package com.kshrd.admsfileservice.employeemanage.model.dto.response;

import com.kshrd.admsfileservice.employeemanage.model.entity.Payroll;
import com.kshrd.admsfileservice.employeemanage.model.enums.PayrollStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollResponse {
    private UUID id;
    private UUID employeeId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal amount;
    private PayrollStatus status;

    public static PayrollResponse from(Payroll payroll) {
        return PayrollResponse.builder()
                .id(payroll.getId())
                .employeeId(payroll.getEmployeeId())
                .periodStart(payroll.getPeriodStart())
                .periodEnd(payroll.getPeriodEnd())
                .amount(payroll.getAmount())
                .status(payroll.getStatus())
                .build();
    }
}

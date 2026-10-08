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
    private BigDecimal basicSalary;
    private BigDecimal allowances;
    private BigDecimal deductions;
    private BigDecimal tax;
    private BigDecimal netAmount;
    private PayrollStatus status;

    public static PayrollResponse from(Payroll payroll) {
        BigDecimal basic = zero(payroll.getBasicSalary() != null ? payroll.getBasicSalary() : payroll.getAmount());
        BigDecimal allowances = zero(payroll.getAllowances());
        BigDecimal deductions = zero(payroll.getDeductions());
        BigDecimal tax = zero(payroll.getTax());
        BigDecimal net = payroll.getAmount() != null
                ? payroll.getAmount()
                : basic.add(allowances).subtract(deductions).subtract(tax);
        return PayrollResponse.builder()
                .id(payroll.getId())
                .employeeId(payroll.getEmployeeId())
                .periodStart(payroll.getPeriodStart())
                .periodEnd(payroll.getPeriodEnd())
                .amount(net)
                .basicSalary(basic)
                .allowances(allowances)
                .deductions(deductions)
                .tax(tax)
                .netAmount(net)
                .status(payroll.getStatus())
                .build();
    }

    private static BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}

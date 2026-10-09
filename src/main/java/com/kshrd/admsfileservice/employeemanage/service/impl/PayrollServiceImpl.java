package com.kshrd.admsfileservice.employeemanage.service.impl;

import com.kshrd.admsfileservice.employeemanage.exception.InvalidOperationException;
import com.kshrd.admsfileservice.employeemanage.exception.ResourceNotFoundException;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.PayrollRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.EmployeeResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.PayrollResponse;
import com.kshrd.admsfileservice.employeemanage.model.entity.Payroll;
import com.kshrd.admsfileservice.employeemanage.model.enums.PayrollStatus;
import com.kshrd.admsfileservice.employeemanage.repository.PayrollRepository;
import com.kshrd.admsfileservice.employeemanage.security.AccessService;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import com.kshrd.admsfileservice.employeemanage.service.NotificationService;
import com.kshrd.admsfileservice.employeemanage.service.PayrollService;
import com.kshrd.admsfileservice.employeemanage.service.StoredDocument;
import com.kshrd.admsfileservice.employeemanage.util.SimplePdf;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class PayrollServiceImpl implements PayrollService {
    private final PayrollRepository payrollRepository;
    private final EmployeeService employeeService;
    private final AccessService accessService;
    private final NotificationService notificationService;

    public PayrollServiceImpl(
            PayrollRepository payrollRepository,
            EmployeeService employeeService,
            AccessService accessService,
            NotificationService notificationService) {
        this.payrollRepository = payrollRepository;
        this.employeeService = employeeService;
        this.accessService = accessService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollResponse> getPayrolls(UUID employeeId, PayrollStatus status) {
        UUID scoped = accessService.resolveEmployeeId(employeeId);
        Set<UUID> visible = accessService.visibleEmployeeIds();
        return payrollRepository.findAll().stream()
                .filter(payroll -> scoped == null || payroll.getEmployeeId().equals(scoped))
                .filter(payroll -> visible == null || visible.contains(payroll.getEmployeeId()))
                .filter(payroll -> status == null || payroll.getStatus() == status)
                .map(PayrollResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollResponse getPayrollById(UUID id) {
        Payroll payroll = findPayroll(id);
        accessService.assertCanViewEmployee(payroll.getEmployeeId());
        return PayrollResponse.from(payroll);
    }

    @Override
    public PayrollResponse createPayroll(PayrollRequest request) {
        employeeService.getEmployeeById(request.getEmployeeId());
        if (request.getPeriodEnd().isBefore(request.getPeriodStart())) {
            throw new InvalidOperationException("Period end cannot be before period start");
        }
        BigDecimal basic = first(request.getBasicSalary(), request.getAmount());
        if (basic == null || basic.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Basic salary or amount is required");
        }
        BigDecimal allowances = zero(request.getAllowances());
        BigDecimal deductions = zero(request.getDeductions());
        BigDecimal tax = zero(request.getTax());
        BigDecimal net = basic.add(allowances).subtract(deductions).subtract(tax);
        if (net.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Net pay must be greater than 0");
        }
        Payroll payroll = Payroll.builder()
                .id(UUID.randomUUID())
                .employeeId(request.getEmployeeId())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .basicSalary(basic)
                .allowances(allowances)
                .deductions(deductions)
                .tax(tax)
                .amount(net)
                .status(PayrollStatus.PENDING)
                .build();
        return PayrollResponse.from(payrollRepository.save(payroll));
    }

    @Override
    public PayrollResponse markPaid(UUID id) {
        Payroll payroll = findPayroll(id);
        if (payroll.getStatus() == PayrollStatus.PAID) {
            throw new InvalidOperationException("Payroll is already paid");
        }
        payroll.setStatus(PayrollStatus.PAID);
        PayrollResponse response = PayrollResponse.from(payrollRepository.save(payroll));
        notificationService.notifyEmployee(
                payroll.getEmployeeId(),
                "Payslip ready",
                "Your payroll for " + payroll.getPeriodStart() + " to " + payroll.getPeriodEnd() + " is marked paid.");
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public StoredDocument getPayslip(UUID id) {
        PayrollResponse payroll = getPayrollById(id);
        EmployeeResponse employee = employeeService.getEmployeeById(payroll.getEmployeeId());
        byte[] pdf = SimplePdf.of("Payslip", List.of(
                "Employee: " + employee.getFirstName() + " " + employee.getLastName(),
                "Department: " + employee.getDepartment() + "  |  " + employee.getPosition(),
                "Period: " + payroll.getPeriodStart() + " to " + payroll.getPeriodEnd(),
                "Status: " + payroll.getStatus(),
                " ",
                "Basic salary: " + payroll.getBasicSalary(),
                "Allowances: " + payroll.getAllowances(),
                "Deductions: " + payroll.getDeductions(),
                "Tax: " + payroll.getTax(),
                "Net pay: " + payroll.getNetAmount()
        ));
        return new StoredDocument(
                new ByteArrayResource(pdf),
                "payslip-" + payroll.getPeriodStart() + ".pdf",
                "application/pdf",
                pdf.length);
    }

    private Payroll findPayroll(UUID id) {
        return payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll", id));
    }

    private BigDecimal first(BigDecimal preferred, BigDecimal fallback) {
        return preferred != null ? preferred : fallback;
    }

    private BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}

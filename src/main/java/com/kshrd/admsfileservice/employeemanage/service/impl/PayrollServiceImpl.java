package com.kshrd.admsfileservice.employeemanage.service.impl;

import com.kshrd.admsfileservice.employeemanage.exception.InvalidOperationException;
import com.kshrd.admsfileservice.employeemanage.exception.ResourceNotFoundException;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.PayrollRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.PayrollResponse;
import com.kshrd.admsfileservice.employeemanage.model.entity.Payroll;
import com.kshrd.admsfileservice.employeemanage.model.enums.PayrollStatus;
import com.kshrd.admsfileservice.employeemanage.repository.PayrollRepository;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import com.kshrd.admsfileservice.employeemanage.service.PayrollService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PayrollServiceImpl implements PayrollService {
    private final PayrollRepository payrollRepository;
    private final EmployeeService employeeService;

    public PayrollServiceImpl(PayrollRepository payrollRepository, EmployeeService employeeService) {
        this.payrollRepository = payrollRepository;
        this.employeeService = employeeService;
    }

    @Override
    public List<PayrollResponse> getPayrolls(UUID employeeId, PayrollStatus status) {
        return payrollRepository.findAll().stream()
                .filter(payroll -> employeeId == null || payroll.getEmployeeId().equals(employeeId))
                .filter(payroll -> status == null || payroll.getStatus() == status)
                .map(PayrollResponse::from)
                .toList();
    }

    @Override
    public PayrollResponse getPayrollById(UUID id) {
        return PayrollResponse.from(findPayroll(id));
    }

    @Override
    public PayrollResponse createPayroll(PayrollRequest request) {
        employeeService.getEmployeeById(request.getEmployeeId());
        if (request.getPeriodEnd().isBefore(request.getPeriodStart())) {
            throw new InvalidOperationException("Period end cannot be before period start");
        }
        Payroll payroll = Payroll.builder()
                .id(UUID.randomUUID())
                .employeeId(request.getEmployeeId())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .amount(request.getAmount())
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
        return PayrollResponse.from(payrollRepository.save(payroll));
    }

    private Payroll findPayroll(UUID id) {
        return payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll", id));
    }
}

package com.kshrd.admsfileservice.employeemanage.service;

import com.kshrd.admsfileservice.employeemanage.model.dto.request.PayrollRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.PayrollResponse;
import com.kshrd.admsfileservice.employeemanage.model.enums.PayrollStatus;

import java.util.List;
import java.util.UUID;

public interface PayrollService {
    List<PayrollResponse> getPayrolls(UUID employeeId, PayrollStatus status);

    PayrollResponse getPayrollById(UUID id);

    PayrollResponse createPayroll(PayrollRequest request);

    PayrollResponse markPaid(UUID id);

    StoredDocument getPayslip(UUID id);
}

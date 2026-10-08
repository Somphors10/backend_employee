package com.kshrd.admsfileservice.employeemanage.service;

import com.kshrd.admsfileservice.employeemanage.model.dto.request.LeaveRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.LeaveBalanceResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.LeaveResponse;
import com.kshrd.admsfileservice.employeemanage.model.enums.LeaveStatus;

import java.util.List;
import java.util.UUID;

public interface LeaveService {
    List<LeaveResponse> getLeaves(UUID employeeId, LeaveStatus status);

    LeaveResponse getLeaveById(UUID id);

    LeaveResponse createLeave(LeaveRequest request);

    LeaveResponse approveLeave(UUID id);

    LeaveResponse rejectLeave(UUID id);

    LeaveResponse cancelLeave(UUID id);

    List<LeaveBalanceResponse> getBalances(UUID employeeId);
}

package com.kshrd.admsfileservice.employeemanage.service.impl;

import com.kshrd.admsfileservice.employeemanage.exception.InvalidOperationException;
import com.kshrd.admsfileservice.employeemanage.exception.LeaveNotFoundException;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.LeaveRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.LeaveResponse;
import com.kshrd.admsfileservice.employeemanage.model.entity.Leave;
import com.kshrd.admsfileservice.employeemanage.model.enums.LeaveStatus;
import com.kshrd.admsfileservice.employeemanage.repository.LeaveRepository;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import com.kshrd.admsfileservice.employeemanage.service.LeaveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class LeaveServiceImpl implements LeaveService {
    private final LeaveRepository leaveRepository;
    private final EmployeeService employeeService;

    public LeaveServiceImpl(LeaveRepository leaveRepository, EmployeeService employeeService) {
        this.leaveRepository = leaveRepository;
        this.employeeService = employeeService;
    }

    @Override
    public List<LeaveResponse> getLeaves(UUID employeeId, LeaveStatus status) {
        return leaveRepository.findAll().stream()
                .filter(leave -> employeeId == null || leave.getEmployeeId().equals(employeeId))
                .filter(leave -> status == null || leave.getStatus() == status)
                .sorted(Comparator.comparing(Leave::getStartDate).reversed())
                .map(LeaveResponse::from)
                .toList();
    }

    @Override
    public LeaveResponse getLeaveById(UUID id) {
        return LeaveResponse.from(findLeave(id));
    }

    @Override
    public LeaveResponse createLeave(LeaveRequest request) {
        employeeService.getEmployeeById(request.getEmployeeId());
        Leave leave = Leave.builder()
                .id(UUID.randomUUID())
                .employeeId(request.getEmployeeId())
                .type(request.getType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason().trim())
                .status(LeaveStatus.PENDING)
                .build();
        return LeaveResponse.from(leaveRepository.save(leave));
    }

    @Override
    public LeaveResponse approveLeave(UUID id) {
        return decideLeave(id, LeaveStatus.APPROVED);
    }

    @Override
    public LeaveResponse rejectLeave(UUID id) {
        return decideLeave(id, LeaveStatus.REJECTED);
    }

    private LeaveResponse decideLeave(UUID id, LeaveStatus status) {
        Leave leave = findLeave(id);
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidOperationException("Only pending leave requests can be decided");
        }
        leave.setStatus(status);
        leave.setDecidedAt(Instant.now());
        return LeaveResponse.from(leaveRepository.save(leave));
    }

    private Leave findLeave(UUID id) {
        return leaveRepository.findById(id)
                .orElseThrow(() -> new LeaveNotFoundException(id));
    }
}

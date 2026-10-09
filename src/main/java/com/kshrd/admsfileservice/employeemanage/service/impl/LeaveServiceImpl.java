package com.kshrd.admsfileservice.employeemanage.service.impl;

import com.kshrd.admsfileservice.employeemanage.exception.InvalidOperationException;
import com.kshrd.admsfileservice.employeemanage.exception.LeaveNotFoundException;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.LeaveRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.LeaveBalanceResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.LeaveResponse;
import com.kshrd.admsfileservice.employeemanage.model.entity.Leave;
import com.kshrd.admsfileservice.employeemanage.model.enums.LeaveStatus;
import com.kshrd.admsfileservice.employeemanage.model.enums.LeaveType;
import com.kshrd.admsfileservice.employeemanage.repository.AppSettingRepository;
import com.kshrd.admsfileservice.employeemanage.repository.LeaveRepository;
import com.kshrd.admsfileservice.employeemanage.security.AccessService;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import com.kshrd.admsfileservice.employeemanage.service.LeaveService;
import com.kshrd.admsfileservice.employeemanage.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class LeaveServiceImpl implements LeaveService {
    private final LeaveRepository leaveRepository;
    private final EmployeeService employeeService;
    private final AccessService accessService;
    private final AppSettingRepository appSettingRepository;
    private final NotificationService notificationService;

    public LeaveServiceImpl(
            LeaveRepository leaveRepository,
            EmployeeService employeeService,
            AccessService accessService,
            AppSettingRepository appSettingRepository,
            NotificationService notificationService) {
        this.leaveRepository = leaveRepository;
        this.employeeService = employeeService;
        this.accessService = accessService;
        this.appSettingRepository = appSettingRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponse> getLeaves(UUID employeeId, LeaveStatus status) {
        UUID scoped = accessService.resolveEmployeeId(employeeId);
        Set<UUID> visible = accessService.visibleEmployeeIds();
        return leaveRepository.findAll().stream()
                .filter(leave -> scoped == null || leave.getEmployeeId().equals(scoped))
                .filter(leave -> visible == null || visible.contains(leave.getEmployeeId()))
                .filter(leave -> status == null || leave.getStatus() == status)
                .sorted(Comparator.comparing(Leave::getStartDate).reversed())
                .map(LeaveResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveResponse getLeaveById(UUID id) {
        Leave leave = findLeave(id);
        accessService.assertCanViewEmployee(leave.getEmployeeId());
        return LeaveResponse.from(leave);
    }

    @Override
    public LeaveResponse createLeave(LeaveRequest request) {
        accessService.assertCanActAsEmployee(request.getEmployeeId());
        employeeService.getEmployeeById(request.getEmployeeId());
        if (request.getType() != LeaveType.UNPAID) {
            int remaining = remainingDays(request.getEmployeeId(), request.getType(), LocalDate.now().getYear());
            long days = daysOf(request.getStartDate(), request.getEndDate());
            if (days > remaining) {
                throw new InvalidOperationException(
                        "Not enough " + request.getType().name().toLowerCase() + " leave. Remaining: " + remaining);
            }
        }
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
        LeaveResponse response = decideLeave(id, LeaveStatus.APPROVED);
        notificationService.notifyEmployee(
                response.getEmployeeId(),
                "Leave approved",
                "Your " + response.getType().name().toLowerCase() + " leave was approved.");
        return response;
    }

    @Override
    public LeaveResponse rejectLeave(UUID id) {
        LeaveResponse response = decideLeave(id, LeaveStatus.REJECTED);
        notificationService.notifyEmployee(
                response.getEmployeeId(),
                "Leave rejected",
                "Your " + response.getType().name().toLowerCase() + " leave was rejected.");
        return response;
    }

    @Override
    public LeaveResponse cancelLeave(UUID id) {
        Leave leave = findLeave(id);
        accessService.assertCanActAsEmployee(leave.getEmployeeId());
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidOperationException("Only pending leave requests can be cancelled");
        }
        leave.setStatus(LeaveStatus.CANCELLED);
        leave.setDecidedAt(Instant.now());
        return LeaveResponse.from(leaveRepository.save(leave));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> getBalances(UUID employeeId) {
        UUID scoped = employeeId == null ? accessService.currentEmployeeId() : employeeId;
        if (scoped == null) {
            throw new InvalidOperationException("Employee id is required");
        }
        accessService.assertCanViewEmployee(scoped);
        employeeService.getEmployeeById(scoped);
        int year = LocalDate.now().getYear();
        return List.of(
                balance(scoped, LeaveType.ANNUAL, year, entitled("leave.annual-days", 18)),
                balance(scoped, LeaveType.SICK, year, entitled("leave.sick-days", 10)),
                balance(scoped, LeaveType.UNPAID, year, 365)
        );
    }

    private LeaveResponse decideLeave(UUID id, LeaveStatus status) {
        Leave leave = findLeave(id);
        accessService.assertCanDecideFor(leave.getEmployeeId());
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidOperationException("Only pending leave requests can be decided");
        }
        leave.setStatus(status);
        leave.setDecidedAt(Instant.now());
        return LeaveResponse.from(leaveRepository.save(leave));
    }

    private LeaveBalanceResponse balance(UUID employeeId, LeaveType type, int year, int entitled) {
        int used = 0;
        int pending = 0;
        for (Leave leave : leaveRepository.findAll()) {
            if (!leave.getEmployeeId().equals(employeeId) || leave.getType() != type) {
                continue;
            }
            if (leave.getStartDate().getYear() != year) {
                continue;
            }
            int days = (int) daysOf(leave.getStartDate(), leave.getEndDate());
            if (leave.getStatus() == LeaveStatus.APPROVED) {
                used += days;
            } else if (leave.getStatus() == LeaveStatus.PENDING) {
                pending += days;
            }
        }
        int remaining = type == LeaveType.UNPAID ? entitled : Math.max(0, entitled - used - pending);
        return LeaveBalanceResponse.builder()
                .employeeId(employeeId)
                .year(year)
                .type(type)
                .entitled(entitled)
                .used(used)
                .pending(pending)
                .remaining(remaining)
                .build();
    }

    private int remainingDays(UUID employeeId, LeaveType type, int year) {
        return balance(employeeId, type, year, type == LeaveType.ANNUAL
                ? entitled("leave.annual-days", 18)
                : entitled("leave.sick-days", 10)).getRemaining();
    }

    private int entitled(String key, int fallback) {
        return appSettingRepository.findBySettingKey(key)
                .map(setting -> {
                    try {
                        return Integer.parseInt(setting.getSettingValue());
                    } catch (NumberFormatException ex) {
                        return fallback;
                    }
                })
                .orElse(fallback);
    }

    private long daysOf(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end) + 1;
    }

    private Leave findLeave(UUID id) {
        return leaveRepository.findById(id)
                .orElseThrow(() -> new LeaveNotFoundException(id));
    }
}

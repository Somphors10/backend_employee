package com.kshrd.admsfileservice.employeemanage.service.impl;

import com.kshrd.admsfileservice.employeemanage.model.dto.response.DashboardResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.NavigationItemResponse;
import com.kshrd.admsfileservice.employeemanage.model.enums.EmploymentStatus;
import com.kshrd.admsfileservice.employeemanage.model.enums.LeaveStatus;
import com.kshrd.admsfileservice.employeemanage.model.enums.PayrollStatus;
import com.kshrd.admsfileservice.employeemanage.repository.AttendanceRepository;
import com.kshrd.admsfileservice.employeemanage.repository.EmployeeRepository;
import com.kshrd.admsfileservice.employeemanage.repository.LeaveRepository;
import com.kshrd.admsfileservice.employeemanage.repository.PayrollRepository;
import com.kshrd.admsfileservice.employeemanage.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {
    private final EmployeeRepository employeeRepository;
    private final LeaveRepository leaveRepository;
    private final AttendanceRepository attendanceRepository;
    private final PayrollRepository payrollRepository;

    public DashboardServiceImpl(
            EmployeeRepository employeeRepository,
            LeaveRepository leaveRepository,
            AttendanceRepository attendanceRepository,
            PayrollRepository payrollRepository) {
        this.employeeRepository = employeeRepository;
        this.leaveRepository = leaveRepository;
        this.attendanceRepository = attendanceRepository;
        this.payrollRepository = payrollRepository;
    }

    @Override
    public DashboardResponse getDashboard() {
        return DashboardResponse.builder()
                .totalEmployees(employeeRepository.count())
                .activeEmployees(employeeRepository.countByStatus(EmploymentStatus.ACTIVE))
                .pendingLeaves(leaveRepository.countByStatus(LeaveStatus.PENDING))
                .todayAttendance(attendanceRepository.countByWorkDate(LocalDate.now()))
                .pendingPayrolls(payrollRepository.countByStatus(PayrollStatus.PENDING))
                .build();
    }

    @Override
    public List<NavigationItemResponse> getNavigation() {
        return List.of(
                item("dashboard", "Dashboard", "/dashboard", "LIVE"),
                item("employees", "Employees", "/employees", "LIVE"),
                item("leaves", "Leaves", "/leaves", "LIVE"),
                item("attendance", "Attendance", "/attendance", "COMING_NEXT"),
                item("payroll", "Payroll", "/payroll", "COMING_NEXT"),
                item("documents", "Documents", "/documents", "COMING_NEXT"),
                item("performance", "Performance", "/performance", "COMING_NEXT"),
                item("organization", "Organization", "/organization", "COMING_NEXT"),
                item("announcements", "Announcements", "/announcements", "COMING_NEXT"),
                item("settings", "Settings", "/settings", "COMING_NEXT")
        );
    }

    private NavigationItemResponse item(String key, String label, String path, String status) {
        return NavigationItemResponse.builder()
                .key(key)
                .label(label)
                .path(path)
                .status(status)
                .build();
    }
}

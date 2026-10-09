package com.kshrd.admsfileservice.employeemanage.service.impl;

import com.kshrd.admsfileservice.employeemanage.model.dto.response.DashboardResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.NavigationItemResponse;
import com.kshrd.admsfileservice.employeemanage.model.entity.Employee;
import com.kshrd.admsfileservice.employeemanage.model.enums.EmploymentStatus;
import com.kshrd.admsfileservice.employeemanage.model.enums.LeaveStatus;
import com.kshrd.admsfileservice.employeemanage.model.enums.PayrollStatus;
import com.kshrd.admsfileservice.employeemanage.model.enums.Role;
import com.kshrd.admsfileservice.employeemanage.repository.AttendanceRepository;
import com.kshrd.admsfileservice.employeemanage.repository.EmployeeRepository;
import com.kshrd.admsfileservice.employeemanage.repository.LeaveRepository;
import com.kshrd.admsfileservice.employeemanage.repository.PayrollRepository;
import com.kshrd.admsfileservice.employeemanage.security.AccessService;
import com.kshrd.admsfileservice.employeemanage.security.Permission;
import com.kshrd.admsfileservice.employeemanage.security.RolePermissions;
import com.kshrd.admsfileservice.employeemanage.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {
    private static final List<NavigationItemResponse> NAV = List.of(
            item("dashboard", "Dashboard", "/dashboard", Permission.DASHBOARD_VIEW),
            item("employees", "Employees", "/employees", Permission.EMPLOYEES_DIRECTORY),
            item("leaves", "Leaves", "/leaves", Permission.LEAVES_VIEW),
            item("attendance", "Attendance", "/attendance", Permission.ATTENDANCE_VIEW),
            item("payroll", "Payroll", "/payroll", Permission.PAYROLL_VIEW),
            item("documents", "Documents", "/documents", Permission.DOCUMENTS_VIEW),
            item("performance", "Performance", "/performance", Permission.PERFORMANCE_VIEW),
            item("organization", "Departments", "/organization", Permission.ORGANIZATION_VIEW),
            item("overtime", "Overtime", "/overtime", Permission.OVERTIME_VIEW),
            item("holidays", "Holidays", "/holidays", Permission.HOLIDAYS_VIEW),
            item("announcements", "Announcements", "/announcements", Permission.ANNOUNCEMENTS_VIEW),
            item("reports", "Reports", "/reports", Permission.REPORTS_VIEW),
            item("users", "Users", "/users", Permission.USERS_WRITE),
            item("roles", "Roles", "/roles", Permission.ROLES_VIEW),
            item("settings", "Settings", "/settings", Permission.SETTINGS_VIEW)
    );

    private final EmployeeRepository employeeRepository;
    private final LeaveRepository leaveRepository;
    private final AttendanceRepository attendanceRepository;
    private final PayrollRepository payrollRepository;
    private final AccessService accessService;

    public DashboardServiceImpl(
            EmployeeRepository employeeRepository,
            LeaveRepository leaveRepository,
            AttendanceRepository attendanceRepository,
            PayrollRepository payrollRepository,
            AccessService accessService) {
        this.employeeRepository = employeeRepository;
        this.leaveRepository = leaveRepository;
        this.attendanceRepository = attendanceRepository;
        this.payrollRepository = payrollRepository;
        this.accessService = accessService;
    }

    @Override
    public DashboardResponse getDashboard() {
        Set<UUID> visible = accessService.visibleEmployeeIds();
        if (visible == null) {
            return DashboardResponse.builder()
                    .totalEmployees(employeeRepository.count())
                    .activeEmployees(employeeRepository.countByStatus(EmploymentStatus.ACTIVE))
                    .pendingLeaves(leaveRepository.countByStatus(LeaveStatus.PENDING))
                    .todayAttendance(attendanceRepository.countByWorkDate(LocalDate.now()))
                    .pendingPayrolls(payrollRepository.countByStatus(PayrollStatus.PENDING))
                    .build();
        }

        List<Employee> people = visible.isEmpty() ? List.of() : employeeRepository.findAllById(visible);
        LocalDate today = LocalDate.now();
        return DashboardResponse.builder()
                .totalEmployees(people.size())
                .activeEmployees(people.stream()
                        .filter(employee -> employee.getStatus() == EmploymentStatus.ACTIVE)
                        .count())
                .pendingLeaves(leaveRepository.findAll().stream()
                        .filter(leave -> visible.contains(leave.getEmployeeId()))
                        .filter(leave -> leave.getStatus() == LeaveStatus.PENDING)
                        .count())
                .todayAttendance(attendanceRepository.findAll().stream()
                        .filter(row -> visible.contains(row.getEmployeeId()))
                        .filter(row -> today.equals(row.getWorkDate()))
                        .count())
                .pendingPayrolls(payrollRepository.findAll().stream()
                        .filter(payroll -> visible.contains(payroll.getEmployeeId()))
                        .filter(payroll -> payroll.getStatus() == PayrollStatus.PENDING)
                        .count())
                .build();
    }

    @Override
    public List<NavigationItemResponse> getNavigation() {
        Role role = accessService.currentRole();
        if (role == null) {
            return NAV;
        }
        return NAV.stream()
                .filter(item -> RolePermissions.has(role, item.getPermission()))
                .toList();
    }

    private static NavigationItemResponse item(String key, String label, String path, String permission) {
        return NavigationItemResponse.builder()
                .key(key)
                .label(label)
                .path(path)
                .status("LIVE")
                .permission(permission)
                .build();
    }
}

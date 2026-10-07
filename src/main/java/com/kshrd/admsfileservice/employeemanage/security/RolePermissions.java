package com.kshrd.admsfileservice.employeemanage.security;

import com.kshrd.admsfileservice.employeemanage.model.enums.Role;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class RolePermissions {
    private static final List<String> EMPLOYEE = List.of(
            Permission.DASHBOARD_VIEW,
            Permission.EMPLOYEES_VIEW,
            Permission.LEAVES_VIEW,
            Permission.LEAVES_CREATE,
            Permission.ATTENDANCE_VIEW,
            Permission.ATTENDANCE_CHECK,
            Permission.DOCUMENTS_VIEW,
            Permission.PERFORMANCE_VIEW,
            Permission.ORGANIZATION_VIEW,
            Permission.ANNOUNCEMENTS_VIEW
    );

    private static final List<String> MANAGER = concat(EMPLOYEE, List.of(
            Permission.LEAVES_DECIDE,
            Permission.PERFORMANCE_WRITE
    ));

    private static final List<String> HR = concat(MANAGER, List.of(
            Permission.EMPLOYEES_WRITE,
            Permission.PAYROLL_VIEW,
            Permission.PAYROLL_WRITE,
            Permission.DOCUMENTS_WRITE,
            Permission.ORGANIZATION_WRITE,
            Permission.ANNOUNCEMENTS_WRITE,
            Permission.SETTINGS_VIEW
    ));

    private static final List<String> ADMIN = concat(HR, List.of(Permission.SETTINGS_WRITE));

    private RolePermissions() {
    }

    public static List<String> forRole(Role role) {
        if (role == null) {
            return List.of();
        }
        return switch (role) {
            case ADMIN -> ADMIN;
            case HR -> HR;
            case MANAGER -> MANAGER;
            case EMPLOYEE -> EMPLOYEE;
        };
    }

    public static boolean has(Role role, String permission) {
        return forRole(role).contains(permission);
    }

    private static List<String> concat(List<String> base, List<String> extra) {
        Set<String> values = new LinkedHashSet<>(base);
        values.addAll(extra);
        return List.copyOf(values);
    }
}

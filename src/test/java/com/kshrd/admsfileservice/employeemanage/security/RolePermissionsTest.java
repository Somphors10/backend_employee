package com.kshrd.admsfileservice.employeemanage.security;

import com.kshrd.admsfileservice.employeemanage.model.enums.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RolePermissionsTest {

    @Test
    void adminHasEveryPermission() {
        assertTrue(RolePermissions.has(Role.ADMIN, Permission.SETTINGS_WRITE));
        assertTrue(RolePermissions.has(Role.ADMIN, Permission.PAYROLL_VIEW));
        assertTrue(RolePermissions.has(Role.ADMIN, Permission.EMPLOYEES_WRITE));
    }

    @Test
    void hrCannotChangeSettings() {
        assertTrue(RolePermissions.has(Role.HR, Permission.SETTINGS_VIEW));
        assertFalse(RolePermissions.has(Role.HR, Permission.SETTINGS_WRITE));
        assertTrue(RolePermissions.has(Role.HR, Permission.EMPLOYEES_WRITE));
    }

    @Test
    void managerCanDecideLeaveButNotPayroll() {
        assertTrue(RolePermissions.has(Role.MANAGER, Permission.LEAVES_DECIDE));
        assertTrue(RolePermissions.has(Role.MANAGER, Permission.PERFORMANCE_WRITE));
        assertFalse(RolePermissions.has(Role.MANAGER, Permission.PAYROLL_VIEW));
        assertFalse(RolePermissions.has(Role.MANAGER, Permission.EMPLOYEES_WRITE));
    }

    @Test
    void employeeCanCreateLeaveButCannotWriteEmployees() {
        assertTrue(RolePermissions.has(Role.EMPLOYEE, Permission.LEAVES_CREATE));
        assertTrue(RolePermissions.has(Role.EMPLOYEE, Permission.ATTENDANCE_CHECK));
        assertFalse(RolePermissions.has(Role.EMPLOYEE, Permission.LEAVES_DECIDE));
        assertFalse(RolePermissions.has(Role.EMPLOYEE, Permission.EMPLOYEES_WRITE));
        assertFalse(RolePermissions.has(Role.EMPLOYEE, Permission.SETTINGS_VIEW));
    }
}

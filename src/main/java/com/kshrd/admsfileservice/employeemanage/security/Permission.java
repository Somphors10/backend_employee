package com.kshrd.admsfileservice.employeemanage.security;

public final class Permission {
    public static final String DASHBOARD_VIEW = "dashboard:view";

    public static final String EMPLOYEES_VIEW = "employees:view";
    public static final String EMPLOYEES_WRITE = "employees:write";

    public static final String LEAVES_VIEW = "leaves:view";
    public static final String LEAVES_CREATE = "leaves:create";
    public static final String LEAVES_DECIDE = "leaves:decide";

    public static final String ATTENDANCE_VIEW = "attendance:view";
    public static final String ATTENDANCE_CHECK = "attendance:check";

    public static final String PAYROLL_VIEW = "payroll:view";
    public static final String PAYROLL_WRITE = "payroll:write";

    public static final String DOCUMENTS_VIEW = "documents:view";
    public static final String DOCUMENTS_WRITE = "documents:write";

    public static final String PERFORMANCE_VIEW = "performance:view";
    public static final String PERFORMANCE_WRITE = "performance:write";

    public static final String ORGANIZATION_VIEW = "organization:view";
    public static final String ORGANIZATION_WRITE = "organization:write";

    public static final String ANNOUNCEMENTS_VIEW = "announcements:view";
    public static final String ANNOUNCEMENTS_WRITE = "announcements:write";

    public static final String SETTINGS_VIEW = "settings:view";
    public static final String SETTINGS_WRITE = "settings:write";

    public static final String USERS_WRITE = "users:write";
    public static final String HOLIDAYS_VIEW = "holidays:view";
    public static final String HOLIDAYS_WRITE = "holidays:write";
    public static final String OVERTIME_VIEW = "overtime:view";
    public static final String OVERTIME_WRITE = "overtime:write";
    public static final String OVERTIME_DECIDE = "overtime:decide";
    public static final String REPORTS_VIEW = "reports:view";
    public static final String NOTIFICATIONS_VIEW = "notifications:view";

    private Permission() {
    }
}

package com.kshrd.admsfileservice.employeemanage.config;

import com.kshrd.admsfileservice.employeemanage.model.entity.Announcement;
import com.kshrd.admsfileservice.employeemanage.model.entity.AppSetting;
import com.kshrd.admsfileservice.employeemanage.model.entity.AppUser;
import com.kshrd.admsfileservice.employeemanage.model.entity.Attendance;
import com.kshrd.admsfileservice.employeemanage.model.entity.Employee;
import com.kshrd.admsfileservice.employeemanage.model.entity.EmployeeDocument;
import com.kshrd.admsfileservice.employeemanage.model.entity.EmployeeHistory;
import com.kshrd.admsfileservice.employeemanage.model.entity.Leave;
import com.kshrd.admsfileservice.employeemanage.model.entity.OrganizationDepartment;
import com.kshrd.admsfileservice.employeemanage.model.entity.Payroll;
import com.kshrd.admsfileservice.employeemanage.model.entity.PerformanceReview;
import com.kshrd.admsfileservice.employeemanage.model.enums.AttendanceStatus;
import com.kshrd.admsfileservice.employeemanage.model.enums.DocumentType;
import com.kshrd.admsfileservice.employeemanage.model.enums.EmployeeEventType;
import com.kshrd.admsfileservice.employeemanage.model.enums.EmploymentStatus;
import com.kshrd.admsfileservice.employeemanage.model.enums.LeaveStatus;
import com.kshrd.admsfileservice.employeemanage.model.enums.LeaveType;
import com.kshrd.admsfileservice.employeemanage.model.enums.PayrollStatus;
import com.kshrd.admsfileservice.employeemanage.model.enums.Role;
import com.kshrd.admsfileservice.employeemanage.repository.AnnouncementRepository;
import com.kshrd.admsfileservice.employeemanage.repository.AppSettingRepository;
import com.kshrd.admsfileservice.employeemanage.repository.AppUserRepository;
import com.kshrd.admsfileservice.employeemanage.repository.AttendanceRepository;
import com.kshrd.admsfileservice.employeemanage.repository.EmployeeDocumentRepository;
import com.kshrd.admsfileservice.employeemanage.repository.EmployeeHistoryRepository;
import com.kshrd.admsfileservice.employeemanage.repository.EmployeeRepository;
import com.kshrd.admsfileservice.employeemanage.repository.LeaveRepository;
import com.kshrd.admsfileservice.employeemanage.repository.OrganizationDepartmentRepository;
import com.kshrd.admsfileservice.employeemanage.repository.PayrollRepository;
import com.kshrd.admsfileservice.employeemanage.repository.PerformanceReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class SampleDataSeeder implements ApplicationRunner {
    private final EmployeeRepository employeeRepository;
    private final EmployeeHistoryRepository employeeHistoryRepository;
    private final LeaveRepository leaveRepository;
    private final AttendanceRepository attendanceRepository;
    private final PayrollRepository payrollRepository;
    private final EmployeeDocumentRepository employeeDocumentRepository;
    private final PerformanceReviewRepository performanceReviewRepository;
    private final OrganizationDepartmentRepository organizationDepartmentRepository;
    private final AnnouncementRepository announcementRepository;
    private final AppSettingRepository appSettingRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Employee> employees = seedEmployees();
        seedHistory(employees);
        seedLeaves(employees);
        seedAttendances(employees);
        seedPayrolls(employees);
        seedDocuments(employees);
        seedReviews(employees);
        seedDepartments(employees);
        seedAnnouncements();
        seedSettings();
        seedUsers(employees);
    }

    private List<Employee> seedEmployees() {
        Employee manager = saveEmployeeIfMissing(
                "Sokha", "Chan", "sokha.chan@company.com", "012111111",
                "HR Manager", "HR", LocalDate.of(2018, 3, 12), EmploymentStatus.ACTIVE, null);

        record Sample(String firstName, String lastName, String email, String phone, String position,
                      String department, LocalDate hireDate, EmploymentStatus status) {
        }

        List<Sample> samples = List.of(
                new Sample("Dara", "Kim", "dara.kim@company.com", "012111112", "Software Engineer", "IT", LocalDate.of(2021, 6, 1), EmploymentStatus.ACTIVE),
                new Sample("Sreyneang", "Pich", "sreyneang.pich@company.com", "012111113", "Accountant", "Finance", LocalDate.of(2020, 1, 15), EmploymentStatus.ACTIVE),
                new Sample("Vicheka", "Meas", "vicheka.meas@company.com", "012111114", "Recruiter", "HR", LocalDate.of(2022, 4, 8), EmploymentStatus.ACTIVE),
                new Sample("Rithy", "Sok", "rithy.sok@company.com", "012111115", "QA Engineer", "IT", LocalDate.of(2023, 2, 20), EmploymentStatus.ACTIVE),
                new Sample("Malis", "Heng", "malis.heng@company.com", "012111116", "Marketing Lead", "Marketing", LocalDate.of(2019, 9, 3), EmploymentStatus.ACTIVE),
                new Sample("Borey", "Lim", "borey.lim@company.com", "012111117", "Sales Executive", "Sales", LocalDate.of(2022, 11, 11), EmploymentStatus.ACTIVE),
                new Sample("Chenda", "Oun", "chenda.oun@company.com", "012111118", "Operations Officer", "Operations", LocalDate.of(2017, 5, 19), EmploymentStatus.INACTIVE),
                new Sample("Pisey", "Nguon", "pisey.nguon@company.com", "012111119", "Designer", "Design", LocalDate.of(2024, 1, 8), EmploymentStatus.ACTIVE),
                new Sample("Arun", "Prak", "arun.prak@company.com", "012111120", "Support Specialist", "Support", LocalDate.of(2023, 8, 25), EmploymentStatus.ACTIVE)
        );

        List<Employee> employees = new ArrayList<>();
        employees.add(manager);
        for (Sample sample : samples) {
            employees.add(saveEmployeeIfMissing(
                    sample.firstName(), sample.lastName(), sample.email(), sample.phone(),
                    sample.position(), sample.department(), sample.hireDate(), sample.status(), manager.getId()));
        }
        return employees;
    }

    private Employee saveEmployeeIfMissing(String firstName, String lastName, String email, String phone,
                                           String position, String department, LocalDate hireDate,
                                           EmploymentStatus status, UUID managerId) {
        return employeeRepository.findByEmailIgnoreCase(email).orElseGet(() -> employeeRepository.save(
                Employee.builder()
                        .id(UUID.randomUUID())
                        .firstName(firstName)
                        .lastName(lastName)
                        .email(email)
                        .phoneNumber(phone)
                        .position(position)
                        .department(department)
                        .hireDate(hireDate)
                        .status(status)
                        .managerId(managerId)
                        .build()));
    }

    private void seedHistory(List<Employee> employees) {
        if (employeeHistoryRepository.count() >= 10) {
            return;
        }
        Instant now = Instant.parse("2026-01-15T08:00:00Z");
        EmployeeEventType[] events = EmployeeEventType.values();
        for (int i = 0; i < employees.size(); i++) {
            Employee employee = employees.get(i);
            employeeHistoryRepository.save(EmployeeHistory.builder()
                    .id(UUID.randomUUID())
                    .employeeId(employee.getId())
                    .eventType(events[i % events.length])
                    .description("Sample history for " + employee.getFirstName() + " " + employee.getLastName())
                    .occurredAt(now.plusSeconds(i * 3600L))
                    .build());
        }
    }

    private void seedLeaves(List<Employee> employees) {
        if (leaveRepository.count() >= 10) {
            return;
        }
        LeaveType[] types = LeaveType.values();
        LeaveStatus[] statuses = LeaveStatus.values();
        for (int i = 0; i < 10; i++) {
            Employee employee = employees.get(i % employees.size());
            LeaveStatus status = statuses[i % statuses.length];
            LocalDate start = LocalDate.of(2026, 3, 1).plusDays(i * 3L);
            leaveRepository.save(Leave.builder()
                    .id(UUID.randomUUID())
                    .employeeId(employee.getId())
                    .type(types[i % types.length])
                    .startDate(start)
                    .endDate(start.plusDays(2))
                    .reason("Sample " + types[i % types.length].name().toLowerCase() + " leave")
                    .status(status)
                    .decidedAt(status == LeaveStatus.PENDING ? null : Instant.parse("2026-03-01T04:00:00Z").plusSeconds(i * 86400L))
                    .build());
        }
    }

    private void seedAttendances(List<Employee> employees) {
        if (attendanceRepository.count() >= 10) {
            return;
        }
        AttendanceStatus[] statuses = AttendanceStatus.values();
        for (int i = 0; i < 10; i++) {
            Employee employee = employees.get(i % employees.size());
            AttendanceStatus status = statuses[i % statuses.length];
            boolean present = status == AttendanceStatus.PRESENT || status == AttendanceStatus.LATE;
            attendanceRepository.save(Attendance.builder()
                    .id(UUID.randomUUID())
                    .employeeId(employee.getId())
                    .workDate(LocalDate.of(2026, 10, 1).plusDays(i))
                    .checkIn(present ? LocalTime.of(status == AttendanceStatus.LATE ? 9 : 8, 15) : null)
                    .checkOut(present ? LocalTime.of(17, 30) : null)
                    .status(status)
                    .build());
        }
    }

    private void seedPayrolls(List<Employee> employees) {
        if (payrollRepository.count() >= 10) {
            return;
        }
        for (int i = 0; i < 10; i++) {
            Employee employee = employees.get(i % employees.size());
            payrollRepository.save(Payroll.builder()
                    .id(UUID.randomUUID())
                    .employeeId(employee.getId())
                    .periodStart(LocalDate.of(2026, 1, 1).plusMonths(i % 6))
                    .periodEnd(LocalDate.of(2026, 1, 31).plusMonths(i % 6))
                    .amount(BigDecimal.valueOf(800 + (i * 150L)))
                    .status(i % 2 == 0 ? PayrollStatus.PAID : PayrollStatus.PENDING)
                    .build());
        }
    }

    private void seedDocuments(List<Employee> employees) {
        if (employeeDocumentRepository.count() >= 10) {
            return;
        }
        DocumentType[] types = DocumentType.values();
        String[] titles = {
                "Employment Contract", "National ID Card", "Java Certificate", "Offer Letter",
                "Passport Copy", "Training Certificate", "NDA Agreement", "Tax Form",
                "Degree Diploma", "Emergency Contact Form"
        };
        Instant uploadedAt = Instant.parse("2026-02-01T09:00:00Z");
        for (int i = 0; i < 10; i++) {
            Employee employee = employees.get(i % employees.size());
            employeeDocumentRepository.save(EmployeeDocument.builder()
                    .id(UUID.randomUUID())
                    .employeeId(employee.getId())
                    .title(titles[i])
                    .fileUrl("https://files.company.local/docs/" + (i + 1) + ".pdf")
                    .documentType(types[i % types.length])
                    .uploadedAt(uploadedAt.plusSeconds(i * 3600L))
                    .build());
        }
    }

    private void seedReviews(List<Employee> employees) {
        if (performanceReviewRepository.count() >= 10) {
            return;
        }
        String[] comments = {
                "Exceeds expectations this quarter.",
                "Solid delivery and good teamwork.",
                "Needs more focus on deadlines.",
                "Strong communication with clients.",
                "Great improvement in code quality.",
                "Reliable and supportive teammate.",
                "Should take more ownership of tasks.",
                "Excellent leadership on the project.",
                "Creative and consistent output.",
                "Handles customer issues promptly."
        };
        for (int i = 0; i < 10; i++) {
            Employee employee = employees.get(i % employees.size());
            Employee reviewer = employees.get(0);
            performanceReviewRepository.save(PerformanceReview.builder()
                    .id(UUID.randomUUID())
                    .employeeId(employee.getId())
                    .reviewer(reviewer.getFirstName() + " " + reviewer.getLastName())
                    .rating(3 + (i % 3))
                    .comments(comments[i])
                    .reviewDate(LocalDate.of(2026, 6, 1).plusDays(i))
                    .build());
        }
    }

    private void seedDepartments(List<Employee> employees) {
        record Dept(String name, String description, int managerIndex) {
        }
        List<Dept> departments = List.of(
                new Dept("HR", "People operations and hiring", 0),
                new Dept("IT", "Software and infrastructure", 1),
                new Dept("Finance", "Accounting and payroll", 2),
                new Dept("Marketing", "Brand and campaigns", 5),
                new Dept("Sales", "Customer accounts", 6),
                new Dept("Operations", "Day-to-day operations", 7),
                new Dept("Design", "Product and visual design", 8),
                new Dept("Support", "Customer support desk", 9),
                new Dept("Legal", "Contracts and compliance", 0),
                new Dept("Research", "Product research", 1)
        );
        for (Dept department : departments) {
            if (organizationDepartmentRepository.findByNameIgnoreCase(department.name()).isPresent()) {
                continue;
            }
            organizationDepartmentRepository.save(OrganizationDepartment.builder()
                    .id(UUID.randomUUID())
                    .name(department.name())
                    .description(department.description())
                    .managerId(employees.get(department.managerIndex()).getId())
                    .build());
        }
    }

    private void seedAnnouncements() {
        if (announcementRepository.count() >= 10) {
            return;
        }
        String[] titles = {
                "Welcome new hires", "Office closed Friday", "Q3 performance reviews",
                "Updated leave policy", "Payroll schedule", "IT maintenance window",
                "Team building event", "Health insurance renewal", "New parking rules",
                "All-hands meeting"
        };
        Instant createdAt = Instant.parse("2026-09-01T02:00:00Z");
        for (int i = 0; i < 10; i++) {
            announcementRepository.save(Announcement.builder()
                    .id(UUID.randomUUID())
                    .title(titles[i])
                    .content("Sample announcement: " + titles[i] + ".")
                    .published(i % 3 != 0)
                    .createdAt(createdAt.plusSeconds(i * 86400L))
                    .build());
        }
    }

    private void seedSettings() {
        record Setting(String key, String value) {
        }
        List<Setting> settings = List.of(
                new Setting("company.name", "Employee Manage"),
                new Setting("company.timezone", "Asia/Phnom_Penh"),
                new Setting("leave.annual-days", "18"),
                new Setting("work.start-time", "08:00"),
                new Setting("work.end-time", "17:30"),
                new Setting("payroll.currency", "USD"),
                new Setting("attendance.late-after", "08:15"),
                new Setting("notification.email", "hr@company.com"),
                new Setting("theme.mode", "light"),
                new Setting("theme.primary", "#0ea5e9"),
                new Setting("theme.primary-light", "#7dd3fc"),
                new Setting("language.default", "en")
        );
        for (Setting setting : settings) {
            if (appSettingRepository.findBySettingKey(setting.key()).isPresent()) {
                continue;
            }
            appSettingRepository.save(AppSetting.builder()
                    .id(UUID.randomUUID())
                    .settingKey(setting.key())
                    .settingValue(setting.value())
                    .build());
        }
    }

    private void seedUsers(List<Employee> employees) {
        UUID managerId = employees.getFirst().getId();
        UUID employeeId = employees.size() > 1 ? employees.get(1).getId() : managerId;
        saveUserIfMissing("admin", "admin123", Role.ADMIN, null);
        saveUserIfMissing("hr", "hr123", Role.HR, managerId);
        saveUserIfMissing("manager", "manager123", Role.MANAGER, managerId);
        saveUserIfMissing("employee", "employee123", Role.EMPLOYEE, employeeId);
    }

    private void saveUserIfMissing(String username, String rawPassword, Role role, UUID employeeId) {
        if (appUserRepository.findByUsernameIgnoreCase(username).isPresent()) {
            return;
        }
        appUserRepository.save(AppUser.builder()
                .id(UUID.randomUUID())
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .employeeId(employeeId)
                .enabled(true)
                .build());
    }
}

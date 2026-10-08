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
import com.kshrd.admsfileservice.employeemanage.model.entity.OvertimeRequest;
import com.kshrd.admsfileservice.employeemanage.model.entity.Payroll;
import com.kshrd.admsfileservice.employeemanage.model.entity.PerformanceReview;
import com.kshrd.admsfileservice.employeemanage.model.entity.PublicHoliday;
import com.kshrd.admsfileservice.employeemanage.model.enums.AttendanceStatus;
import com.kshrd.admsfileservice.employeemanage.model.enums.DocumentType;
import com.kshrd.admsfileservice.employeemanage.model.enums.EmployeeEventType;
import com.kshrd.admsfileservice.employeemanage.model.enums.EmploymentStatus;
import com.kshrd.admsfileservice.employeemanage.model.enums.LeaveStatus;
import com.kshrd.admsfileservice.employeemanage.model.enums.LeaveType;
import com.kshrd.admsfileservice.employeemanage.model.enums.PayrollStatus;
import com.kshrd.admsfileservice.employeemanage.model.enums.RequestStatus;
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
import com.kshrd.admsfileservice.employeemanage.repository.OvertimeRequestRepository;
import com.kshrd.admsfileservice.employeemanage.repository.PayrollRepository;
import com.kshrd.admsfileservice.employeemanage.repository.PerformanceReviewRepository;
import com.kshrd.admsfileservice.employeemanage.repository.PublicHolidayRepository;
import com.kshrd.admsfileservice.employeemanage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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
    private final FileStorageService fileStorageService;
    private final PerformanceReviewRepository performanceReviewRepository;
    private final OrganizationDepartmentRepository organizationDepartmentRepository;
    private final AnnouncementRepository announcementRepository;
    private final AppSettingRepository appSettingRepository;
    private final AppUserRepository appUserRepository;
    private final PublicHolidayRepository publicHolidayRepository;
    private final OvertimeRequestRepository overtimeRequestRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Employee> employees = seedEmployees();
        seedEmployeeProfiles(employees);
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
        seedHolidays();
        seedOvertimes(employees);
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

    private void seedEmployeeProfiles(List<Employee> employees) {
        for (int i = 0; i < employees.size(); i++) {
            Employee employee = employees.get(i);
            boolean dirty = false;
            if (employee.getNationalId() == null || employee.getNationalId().isBlank()) {
                employee.setNationalId("01012345" + (10 + i));
                dirty = true;
            }
            if (employee.getDateOfBirth() == null) {
                employee.setDateOfBirth(LocalDate.of(1988, 3, 12).plusYears(i).plusDays(i * 5L));
                dirty = true;
            }
            if (employee.getAddress() == null || employee.getAddress().isBlank()) {
                employee.setAddress("Street " + (100 + i) + ", Phnom Penh");
                dirty = true;
            }
            if (employee.getSalary() == null) {
                employee.setSalary(BigDecimal.valueOf(900 + (i * 80L)));
                dirty = true;
            }
            if (dirty) {
                employees.set(i, employeeRepository.save(employee));
            }
        }
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
            UUID id = UUID.randomUUID();
            String originalName = titles[i].toLowerCase().replace(' ', '-') + ".pdf";
            byte[] pdf = samplePdf(titles[i], employee);
            String storedName = fileStorageService.storeContent(id, originalName, pdf);
            employeeDocumentRepository.save(EmployeeDocument.builder()
                    .id(id)
                    .employeeId(employee.getId())
                    .title(titles[i])
                    .fileUrl("/api/v1/documents/" + id + "/file")
                    .originalFileName(originalName)
                    .storedFileName(storedName)
                    .contentType("application/pdf")
                    .fileSize((long) pdf.length)
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
                new Setting("leave.sick-days", "10"),
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

    private void seedHolidays() {
        if (publicHolidayRepository.count() >= 8) {
            return;
        }
        record Day(String name, LocalDate date) {
        }
        List.of(
                new Day("International New Year", LocalDate.of(2026, 1, 1)),
                new Day("Victory over Genocide Day", LocalDate.of(2026, 1, 7)),
                new Day("International Women's Day", LocalDate.of(2026, 3, 8)),
                new Day("Khmer New Year", LocalDate.of(2026, 4, 14)),
                new Day("Royal Plowing Ceremony", LocalDate.of(2026, 5, 15)),
                new Day("King's Birthday", LocalDate.of(2026, 5, 14)),
                new Day("Constitution Day", LocalDate.of(2026, 9, 24)),
                new Day("Independence Day", LocalDate.of(2026, 11, 9))
        ).forEach(day -> {
            if (publicHolidayRepository.findByHolidayDate(day.date()).isPresent()) {
                return;
            }
            publicHolidayRepository.save(PublicHoliday.builder()
                    .id(UUID.randomUUID())
                    .name(day.name())
                    .holidayDate(day.date())
                    .paid(true)
                    .build());
        });
    }

    private void seedOvertimes(List<Employee> employees) {
        if (overtimeRequestRepository.count() >= 8) {
            return;
        }
        for (int i = 0; i < 8; i++) {
            Employee employee = employees.get(i % employees.size());
            overtimeRequestRepository.save(OvertimeRequest.builder()
                    .id(UUID.randomUUID())
                    .employeeId(employee.getId())
                    .workDate(LocalDate.of(2026, 9, 1).plusDays(i * 3L))
                    .hours(BigDecimal.valueOf(2 + (i % 3)))
                    .reason("Project delivery support")
                    .status(i % 3 == 0 ? RequestStatus.APPROVED : RequestStatus.PENDING)
                    .build());
        }
    }

    private static List<String> sampleDocumentLines(String title, Employee employee) {
        String name = employee.getFirstName() + " " + employee.getLastName();
        String email = employee.getEmail();
        String phone = employee.getPhoneNumber();
        String position = employee.getPosition();
        String department = employee.getDepartment();
        String hire = employee.getHireDate() == null ? "2021-06-01" : employee.getHireDate().toString();
        return switch (title) {
            case "Employment Contract" -> List.of(
                    "Company: Employee Manage Co., Ltd.",
                    "Employee: " + name,
                    "Position: " + position + "  |  Department: " + department,
                    "Start date: " + hire + "  |  Salary: 1,200 USD / month",
                    "Working hours: Monday-Friday, 08:00-17:30  |  Probation: 3 months",
                    "",
                    "1. The employee shall perform the duties of the role above.",
                    "2. Either party may end this contract with 30 days written notice.",
                    "3. Confidential company information must not be disclosed.",
                    "4. Company property must be returned on the last working day.",
                    "5. Annual leave is 18 days, subject to manager approval.",
                    "",
                    "Signed in Phnom Penh on 1 February 2026.",
                    "Employer: Sokha Chan, HR Manager",
                    "Employee: " + name
            );
            case "National ID Card" -> List.of(
                    "Kingdom of Cambodia  |  National Identity Card",
                    "Full name: " + name,
                    "Date of birth: 12 March 1996",
                    "Sex: --    Place of birth: Phnom Penh",
                    "ID number: 010123456" + Math.abs(name.hashCode() % 1000),
                    "Address: Street 271, Sangkat Boeung Tumpun, Phnom Penh",
                    "Issue date: 15 Jan 2022    Expiry date: 15 Jan 2032",
                    "",
                    "This copy is kept on the employee file for HR verification."
            );
            case "Java Certificate" -> List.of(
                    "Certificate of Completion",
                    "This certifies that",
                    name,
                    "has successfully completed",
                    "Java Programming Professional Course",
                    "Duration: 120 hours    Grade: A",
                    "Issued by: KSHRD Training Center",
                    "Certificate no: JAVA-2025-" + Math.abs(name.hashCode() % 9000 + 1000),
                    "Date: 20 December 2025",
                    "",
                    "Director of Training"
            );
            case "Offer Letter" -> List.of(
                    "Employee Manage Co., Ltd.",
                    "Date: 15 January 2026",
                    "",
                    "Dear " + name + ",",
                    "We are pleased to offer you the position of " + position, "in the " + department + " department.",
                    "Start date: " + hire,
                    "Monthly salary: 1,200 USD",
                    "Benefits: NSSF, 18 days annual leave, and health insurance.",
                    "Please sign and return this letter within 7 days.",
                    "",
                    "Sincerely,",
                    "Sokha Chan  |  HR Manager",
                    "hr@company.com"
            );
            case "Passport Copy" -> List.of(
                    "Passport biodata page copy",
                    "Surname: " + employee.getLastName(),
                    "Given names: " + employee.getFirstName(),
                    "Nationality: Cambodian",
                    "Passport no: N01234" + Math.abs(name.hashCode() % 900 + 100),
                    "Date of birth: 12 March 1996",
                    "Date of issue: 02 Apr 2023    Date of expiry: 02 Apr 2033",
                    "Authority: Ministry of Foreign Affairs",
                    "",
                    "Certified true copy for employment records only."
            );
            case "Training Certificate" -> List.of(
                    "Certificate of Attendance",
                    name,
                    "attended the internal training:",
                    "Workplace Safety and Data Protection 2026",
                    "Date: 10 January 2026    Hours: 8",
                    "Venue: Employee Manage head office, Phnom Penh",
                    "Trainer: HR Department",
                    "",
                    "This certificate is stored on the employee training file."
            );
            case "NDA Agreement" -> List.of(
                    "Non-Disclosure Agreement",
                    "Between Employee Manage Co., Ltd. and " + name,
                    "Effective date: " + hire,
                    "",
                    "1. The employee will keep trade secrets and client data confidential.",
                    "2. This duty continues for 2 years after employment ends.",
                    "3. Allowed disclosure: by law or with written company consent.",
                    "4. Breach may lead to disciplinary action and legal remedy.",
                    "",
                    "Company: Sokha Chan, HR Manager",
                    "Employee: " + name + "    Email: " + email
            );
            case "Tax Form" -> List.of(
                    "Employee Tax Information Form 2026",
                    "Employee: " + name,
                    "Position: " + position + "    Department: " + department,
                    "Tax ID: KHM-" + Math.abs(name.hashCode() % 900000 + 100000),
                    "Residency: Cambodia",
                    "Monthly taxable salary: 1,200 USD",
                    "Allowances: NSSF employee contribution as per law",
                    "Bank: ABA    Account name: " + name,
                    "",
                    "I confirm the information above is correct.",
                    "Signature: " + name + "    Date: 1 February 2026"
            );
            case "Degree Diploma" -> List.of(
                    "Bachelor of Science",
                    "This diploma is awarded to",
                    name,
                    "for the completion of Information Technology",
                    "Royal University of Phnom Penh",
                    "Conferred: 15 August 2020    GPA: 3.42 / 4.00",
                    "Diploma no: RUPP-IT-2020-" + Math.abs(name.hashCode() % 900 + 100),
                    "",
                    "Copy retained by HR for recruitment records."
            );
            case "Emergency Contact Form" -> List.of(
                    "Emergency Contact Form",
                    "Employee: " + name,
                    "Phone: " + phone + "    Email: " + email,
                    "Department: " + department + "    Position: " + position,
                    "",
                    "Primary contact: Kim Sothea    Relationship: Sibling",
                    "Phone: 012888001    Address: Toul Kork, Phnom Penh",
                    "Secondary contact: Chan Dara    Relationship: Friend",
                    "Phone: 012888002",
                    "Blood type: O+    Allergies: None recorded",
                    "",
                    "Employee signature: " + name + "    Date: 1 February 2026"
            );
            default -> List.of(
                    "Document owner: " + name,
                    "Department: " + department,
                    "Email: " + email,
                    "This is a sample file stored in Employee Manage."
            );
        };
    }

    private static byte[] samplePdf(String title, Employee employee) {
        StringBuilder content = new StringBuilder();
        content.append("BT\n/F1 18 Tf\n72 740 Td\n(")
                .append(pdfEscape(title))
                .append(") Tj\n/F1 11 Tf\n15 TL\n0 -28 Td\n");
        for (String line : sampleDocumentLines(title, employee)) {
            content.append("(").append(pdfEscape(line.isBlank() ? " " : line)).append(") '\n");
        }
        content.append("ET");
        byte[] stream = content.toString().getBytes(StandardCharsets.ISO_8859_1);
        try {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            int[] offsets = new int[6];
            out.write(new byte[] {'%', 'P', 'D', 'F', '-', '1', '.', '4', '\n',
                    '%', (byte) 0xE2, (byte) 0xE3, (byte) 0xCF, (byte) 0xD3, '\n'});
            offsets[1] = out.size();
            out.write("1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n".getBytes(StandardCharsets.ISO_8859_1));
            offsets[2] = out.size();
            out.write("2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n".getBytes(StandardCharsets.ISO_8859_1));
            offsets[3] = out.size();
            out.write(("3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]"
                    + "/Contents 4 0 R/Resources<</Font<</F1 5 0 R>>>>>>endobj\n")
                    .getBytes(StandardCharsets.ISO_8859_1));
            offsets[4] = out.size();
            out.write(("4 0 obj<</Length " + stream.length + ">>stream\n").getBytes(StandardCharsets.ISO_8859_1));
            out.write(stream);
            out.write("\nendstream\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));
            offsets[5] = out.size();
            out.write("5 0 obj<</Type/Font/Subtype/Type1/BaseFont/Helvetica>>endobj\n"
                    .getBytes(StandardCharsets.ISO_8859_1));
            int xrefAt = out.size();
            out.write("xref\n0 6\n0000000000 65535 f \n".getBytes(StandardCharsets.ISO_8859_1));
            for (int i = 1; i <= 5; i++) {
                out.write("%010d 00000 n \n".formatted(offsets[i]).getBytes(StandardCharsets.ISO_8859_1));
            }
            out.write(("trailer<</Size 6/Root 1 0 R>>\nstartxref\n" + xrefAt + "\n%%EOF\n")
                    .getBytes(StandardCharsets.ISO_8859_1));
            return out.toByteArray();
        } catch (java.io.IOException ex) {
            throw new IllegalStateException("Could not build sample PDF", ex);
        }
    }

    private static String pdfEscape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
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

package com.kshrd.admsfileservice.employeemanage.service.impl;

import com.kshrd.admsfileservice.employeemanage.exception.InvalidOperationException;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.NamedCountResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.PayrollResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.ReportOverviewResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.ReportOverviewResponse.AttendanceReport;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.ReportOverviewResponse.LeaveReport;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.ReportOverviewResponse.OvertimeReport;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.ReportOverviewResponse.PayrollReport;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.ReportOverviewResponse.PeopleReport;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.ReportOverviewResponse.PerformanceReport;
import com.kshrd.admsfileservice.employeemanage.model.entity.Attendance;
import com.kshrd.admsfileservice.employeemanage.model.entity.Employee;
import com.kshrd.admsfileservice.employeemanage.model.entity.Leave;
import com.kshrd.admsfileservice.employeemanage.model.entity.OvertimeRequest;
import com.kshrd.admsfileservice.employeemanage.model.entity.Payroll;
import com.kshrd.admsfileservice.employeemanage.model.entity.PerformanceReview;
import com.kshrd.admsfileservice.employeemanage.model.enums.AttendanceStatus;
import com.kshrd.admsfileservice.employeemanage.model.enums.EmploymentStatus;
import com.kshrd.admsfileservice.employeemanage.model.enums.LeaveStatus;
import com.kshrd.admsfileservice.employeemanage.model.enums.PayrollStatus;
import com.kshrd.admsfileservice.employeemanage.model.enums.RequestStatus;
import com.kshrd.admsfileservice.employeemanage.repository.AttendanceRepository;
import com.kshrd.admsfileservice.employeemanage.repository.EmployeeRepository;
import com.kshrd.admsfileservice.employeemanage.repository.LeaveRepository;
import com.kshrd.admsfileservice.employeemanage.repository.OvertimeRequestRepository;
import com.kshrd.admsfileservice.employeemanage.repository.PayrollRepository;
import com.kshrd.admsfileservice.employeemanage.repository.PerformanceReviewRepository;
import com.kshrd.admsfileservice.employeemanage.service.ReportService;
import com.kshrd.admsfileservice.employeemanage.service.StoredDocument;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {
    private final EmployeeRepository employeeRepository;
    private final LeaveRepository leaveRepository;
    private final AttendanceRepository attendanceRepository;
    private final PayrollRepository payrollRepository;
    private final OvertimeRequestRepository overtimeRequestRepository;
    private final PerformanceReviewRepository performanceReviewRepository;

    public ReportServiceImpl(
            EmployeeRepository employeeRepository,
            LeaveRepository leaveRepository,
            AttendanceRepository attendanceRepository,
            PayrollRepository payrollRepository,
            OvertimeRequestRepository overtimeRequestRepository,
            PerformanceReviewRepository performanceReviewRepository) {
        this.employeeRepository = employeeRepository;
        this.leaveRepository = leaveRepository;
        this.attendanceRepository = attendanceRepository;
        this.payrollRepository = payrollRepository;
        this.overtimeRequestRepository = overtimeRequestRepository;
        this.performanceReviewRepository = performanceReviewRepository;
    }

    @Override
    public ReportOverviewResponse getOverview(LocalDate from, LocalDate to) {
        LocalDate[] range = range(from, to);
        LocalDate start = range[0];
        LocalDate end = range[1];
        List<Employee> employees = employeeRepository.findAll();
        List<Leave> leaves = leaveRepository.findAll().stream()
                .filter(leave -> overlaps(leave.getStartDate(), leave.getEndDate(), start, end))
                .toList();
        List<Attendance> attendances = attendanceRepository.findAll().stream()
                .filter(row -> inRange(row.getWorkDate(), start, end))
                .toList();
        List<Payroll> payrolls = payrollRepository.findAll().stream()
                .filter(row -> overlaps(row.getPeriodStart(), row.getPeriodEnd(), start, end))
                .toList();
        List<OvertimeRequest> overtimes = overtimeRequestRepository.findAll().stream()
                .filter(row -> inRange(row.getWorkDate(), start, end))
                .toList();
        List<PerformanceReview> reviews = performanceReviewRepository.findAll().stream()
                .filter(row -> inRange(row.getReviewDate(), start, end))
                .toList();

        PeopleReport people = peopleReport(employees, start, end);
        LeaveReport leaveReport = leaveReport(leaves);
        AttendanceReport attendanceReport = attendanceReport(attendances);
        PayrollReport payrollReport = payrollReport(payrolls);
        OvertimeReport overtimeReport = overtimeReport(overtimes);
        PerformanceReport performanceReport = performanceReport(reviews);

        return ReportOverviewResponse.builder()
                .from(start)
                .to(end)
                .generatedAt(Instant.now())
                .employees(people.getTotal())
                .pendingLeaves(leaveReport.getPending())
                .todayAttendance(attendanceRepository.countByWorkDate(LocalDate.now()))
                .pendingPayrolls(payrollReport.getPending())
                .pendingOvertimes(overtimeReport.getPending())
                .people(people)
                .leaves(leaveReport)
                .attendance(attendanceReport)
                .payroll(payrollReport)
                .overtime(overtimeReport)
                .performance(performanceReport)
                .build();
    }

    @Override
    public StoredDocument exportCsv(String type, LocalDate from, LocalDate to) {
        LocalDate[] range = range(from, to);
        LocalDate start = range[0];
        LocalDate end = range[1];
        Map<UUID, Employee> people = employeeRepository.findAll().stream()
                .collect(Collectors.toMap(Employee::getId, Function.identity()));
        String kind = type == null ? "people" : type.trim().toLowerCase(Locale.ROOT);
        String csv = switch (kind) {
            case "people" -> peopleCsv(people.values().stream().toList());
            case "leaves" -> leavesCsv(leaveRepository.findAll().stream()
                    .filter(leave -> overlaps(leave.getStartDate(), leave.getEndDate(), start, end))
                    .toList(), people);
            case "attendance" -> attendanceCsv(attendanceRepository.findAll().stream()
                    .filter(row -> inRange(row.getWorkDate(), start, end))
                    .toList(), people);
            case "payrolls", "payroll" -> payrollCsv(payrollRepository.findAll().stream()
                    .filter(row -> overlaps(row.getPeriodStart(), row.getPeriodEnd(), start, end))
                    .toList(), people);
            case "overtimes", "overtime" -> overtimeCsv(overtimeRequestRepository.findAll().stream()
                    .filter(row -> inRange(row.getWorkDate(), start, end))
                    .toList(), people);
            case "performance" -> performanceCsv(performanceReviewRepository.findAll().stream()
                    .filter(row -> inRange(row.getReviewDate(), start, end))
                    .toList(), people);
            default -> throw new InvalidOperationException(
                    "Export type must be people, leaves, attendance, payrolls, overtimes, or performance");
        };
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        return new StoredDocument(
                new ByteArrayResource(bytes),
                "hr-" + kind + "-" + start + "-to-" + end + ".csv",
                "text/csv",
                bytes.length);
    }

    private PeopleReport peopleReport(List<Employee> employees, LocalDate start, LocalDate end) {
        long active = employees.stream().filter(item -> item.getStatus() == EmploymentStatus.ACTIVE).count();
        long newHires = employees.stream()
                .filter(item -> inRange(item.getHireDate(), start, end))
                .count();
        return PeopleReport.builder()
                .total(employees.size())
                .active(active)
                .inactive(employees.size() - active)
                .newHires(newHires)
                .byDepartment(namedCounts(employees.stream()
                        .collect(Collectors.groupingBy(
                                item -> blankToUnknown(item.getDepartment()),
                                TreeMap::new,
                                Collectors.counting()))))
                .byStatus(namedCounts(employees.stream()
                        .collect(Collectors.groupingBy(
                                item -> item.getStatus().name(),
                                LinkedHashMap::new,
                                Collectors.counting()))))
                .byPosition(namedCounts(employees.stream()
                        .collect(Collectors.groupingBy(
                                item -> blankToUnknown(item.getPosition()),
                                TreeMap::new,
                                Collectors.counting()))))
                .build();
    }

    private LeaveReport leaveReport(List<Leave> leaves) {
        long pending = countStatus(leaves, LeaveStatus.PENDING);
        long approved = countStatus(leaves, LeaveStatus.APPROVED);
        long rejected = countStatus(leaves, LeaveStatus.REJECTED);
        long cancelled = countStatus(leaves, LeaveStatus.CANCELLED);
        long approvedDays = leaves.stream()
                .filter(leave -> leave.getStatus() == LeaveStatus.APPROVED)
                .mapToLong(leave -> days(leave.getStartDate(), leave.getEndDate()))
                .sum();
        return LeaveReport.builder()
                .total(leaves.size())
                .pending(pending)
                .approved(approved)
                .rejected(rejected)
                .cancelled(cancelled)
                .approvedDays(approvedDays)
                .byType(namedCounts(leaves.stream()
                        .collect(Collectors.groupingBy(leave -> leave.getType().name(), Collectors.counting()))))
                .byStatus(namedCounts(leaves.stream()
                        .collect(Collectors.groupingBy(leave -> leave.getStatus().name(), Collectors.counting()))))
                .build();
    }

    private AttendanceReport attendanceReport(List<Attendance> rows) {
        long present = countAttendance(rows, AttendanceStatus.PRESENT);
        long absent = countAttendance(rows, AttendanceStatus.ABSENT);
        long late = countAttendance(rows, AttendanceStatus.LATE);
        long onLeave = countAttendance(rows, AttendanceStatus.ON_LEAVE);
        return AttendanceReport.builder()
                .records(rows.size())
                .present(present)
                .absent(absent)
                .late(late)
                .onLeave(onLeave)
                .byStatus(namedCounts(rows.stream()
                        .collect(Collectors.groupingBy(
                                row -> row.getStatus() == null ? "UNKNOWN" : row.getStatus().name(),
                                Collectors.counting()))))
                .build();
    }

    private PayrollReport payrollReport(List<Payroll> rows) {
        long pending = rows.stream().filter(row -> row.getStatus() == PayrollStatus.PENDING).count();
        long paid = rows.stream().filter(row -> row.getStatus() == PayrollStatus.PAID).count();
        BigDecimal pendingAmount = sumPayroll(rows, PayrollStatus.PENDING);
        BigDecimal paidAmount = sumPayroll(rows, PayrollStatus.PAID);
        return PayrollReport.builder()
                .records(rows.size())
                .pending(pending)
                .paid(paid)
                .pendingAmount(pendingAmount)
                .paidAmount(paidAmount)
                .totalAmount(pendingAmount.add(paidAmount))
                .byStatus(namedCounts(rows.stream()
                        .collect(Collectors.groupingBy(row -> row.getStatus().name(), Collectors.counting()))))
                .build();
    }

    private OvertimeReport overtimeReport(List<OvertimeRequest> rows) {
        long pending = rows.stream().filter(row -> row.getStatus() == RequestStatus.PENDING).count();
        long approved = rows.stream().filter(row -> row.getStatus() == RequestStatus.APPROVED).count();
        long rejected = rows.stream().filter(row -> row.getStatus() == RequestStatus.REJECTED).count();
        return OvertimeReport.builder()
                .records(rows.size())
                .pending(pending)
                .approved(approved)
                .rejected(rejected)
                .pendingHours(sumHours(rows, RequestStatus.PENDING))
                .approvedHours(sumHours(rows, RequestStatus.APPROVED))
                .byStatus(namedCounts(rows.stream()
                        .collect(Collectors.groupingBy(row -> row.getStatus().name(), Collectors.counting()))))
                .build();
    }

    private PerformanceReport performanceReport(List<PerformanceReview> rows) {
        double average = rows.isEmpty()
                ? 0
                : rows.stream().mapToInt(PerformanceReview::getRating).average().orElse(0);
        Map<String, Long> byRating = new TreeMap<>();
        rows.forEach(row -> byRating.merge(String.valueOf(row.getRating()), 1L, Long::sum));
        return PerformanceReport.builder()
                .reviews(rows.size())
                .averageRating(BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP).doubleValue())
                .byRating(namedCounts(byRating))
                .build();
    }

    private String peopleCsv(List<Employee> employees) {
        StringBuilder csv = new StringBuilder("Name,Email,Department,Position,Status,Hire date\n");
        employees.stream()
                .sorted(Comparator.comparing(Employee::getLastName).thenComparing(Employee::getFirstName))
                .forEach(employee -> csv.append(String.join(",",
                        csvCell(fullName(employee)),
                        csvCell(employee.getEmail()),
                        csvCell(employee.getDepartment()),
                        csvCell(employee.getPosition()),
                        csvCell(employee.getStatus().name()),
                        csvCell(String.valueOf(employee.getHireDate()))
                )).append('\n'));
        return csv.toString();
    }

    private String leavesCsv(List<Leave> leaves, Map<UUID, Employee> people) {
        StringBuilder csv = new StringBuilder("Employee,Type,Start,End,Days,Status,Reason\n");
        leaves.stream()
                .sorted(Comparator.comparing(Leave::getStartDate).reversed())
                .forEach(leave -> csv.append(String.join(",",
                        csvCell(fullName(people.get(leave.getEmployeeId()))),
                        csvCell(leave.getType().name()),
                        csvCell(String.valueOf(leave.getStartDate())),
                        csvCell(String.valueOf(leave.getEndDate())),
                        csvCell(String.valueOf(days(leave.getStartDate(), leave.getEndDate()))),
                        csvCell(leave.getStatus().name()),
                        csvCell(leave.getReason())
                )).append('\n'));
        return csv.toString();
    }

    private String attendanceCsv(List<Attendance> rows, Map<UUID, Employee> people) {
        StringBuilder csv = new StringBuilder("Employee,Date,Status,Check in,Check out\n");
        rows.stream()
                .sorted(Comparator.comparing(Attendance::getWorkDate).reversed())
                .forEach(row -> csv.append(String.join(",",
                        csvCell(fullName(people.get(row.getEmployeeId()))),
                        csvCell(String.valueOf(row.getWorkDate())),
                        csvCell(row.getStatus().name()),
                        csvCell(row.getCheckIn() == null ? "" : String.valueOf(row.getCheckIn())),
                        csvCell(row.getCheckOut() == null ? "" : String.valueOf(row.getCheckOut()))
                )).append('\n'));
        return csv.toString();
    }

    private String payrollCsv(List<Payroll> rows, Map<UUID, Employee> people) {
        StringBuilder csv = new StringBuilder("Employee,Period start,Period end,Net amount,Status\n");
        rows.stream()
                .sorted(Comparator.comparing(Payroll::getPeriodStart).reversed())
                .forEach(row -> csv.append(String.join(",",
                        csvCell(fullName(people.get(row.getEmployeeId()))),
                        csvCell(String.valueOf(row.getPeriodStart())),
                        csvCell(String.valueOf(row.getPeriodEnd())),
                        csvCell(String.valueOf(netPayroll(row))),
                        csvCell(row.getStatus().name())
                )).append('\n'));
        return csv.toString();
    }

    private String overtimeCsv(List<OvertimeRequest> rows, Map<UUID, Employee> people) {
        StringBuilder csv = new StringBuilder("Employee,Date,Hours,Status,Reason\n");
        rows.stream()
                .sorted(Comparator.comparing(OvertimeRequest::getWorkDate).reversed())
                .forEach(row -> csv.append(String.join(",",
                        csvCell(fullName(people.get(row.getEmployeeId()))),
                        csvCell(String.valueOf(row.getWorkDate())),
                        csvCell(String.valueOf(row.getHours())),
                        csvCell(row.getStatus().name()),
                        csvCell(row.getReason())
                )).append('\n'));
        return csv.toString();
    }

    private String performanceCsv(List<PerformanceReview> rows, Map<UUID, Employee> people) {
        StringBuilder csv = new StringBuilder("Employee,Reviewer,Rating,Date,Comments\n");
        rows.stream()
                .sorted(Comparator.comparing(PerformanceReview::getReviewDate).reversed())
                .forEach(row -> csv.append(String.join(",",
                        csvCell(fullName(people.get(row.getEmployeeId()))),
                        csvCell(row.getReviewer()),
                        csvCell(String.valueOf(row.getRating())),
                        csvCell(String.valueOf(row.getReviewDate())),
                        csvCell(row.getComments())
                )).append('\n'));
        return csv.toString();
    }

    private LocalDate[] range(LocalDate from, LocalDate to) {
        LocalDate end = to == null ? LocalDate.now() : to;
        LocalDate start = from == null ? end.withDayOfYear(1) : from;
        if (start.isAfter(end)) {
            throw new InvalidOperationException("From date cannot be after to date");
        }
        return new LocalDate[]{start, end};
    }

    private boolean inRange(LocalDate value, LocalDate from, LocalDate to) {
        return value != null && !value.isBefore(from) && !value.isAfter(to);
    }

    private boolean overlaps(LocalDate start, LocalDate end, LocalDate from, LocalDate to) {
        return start != null && end != null && !start.isAfter(to) && !end.isBefore(from);
    }

    private long days(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start, end) + 1;
    }

    private long countStatus(List<Leave> leaves, LeaveStatus status) {
        return leaves.stream().filter(leave -> leave.getStatus() == status).count();
    }

    private long countAttendance(List<Attendance> rows, AttendanceStatus status) {
        return rows.stream().filter(row -> row.getStatus() == status).count();
    }

    private BigDecimal sumPayroll(List<Payroll> rows, PayrollStatus status) {
        return rows.stream()
                .filter(row -> row.getStatus() == status)
                .map(this::netPayroll)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal netPayroll(Payroll payroll) {
        return PayrollResponse.from(payroll).getNetAmount();
    }

    private BigDecimal sumHours(List<OvertimeRequest> rows, RequestStatus status) {
        return rows.stream()
                .filter(row -> row.getStatus() == status)
                .map(row -> row.getHours() == null ? BigDecimal.ZERO : row.getHours())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<NamedCountResponse> namedCounts(Map<String, Long> counts) {
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(entry -> NamedCountResponse.builder()
                        .name(entry.getKey())
                        .count(entry.getValue())
                        .percent(total == 0 ? 0 : Math.round(entry.getValue() * 1000.0 / total) / 10.0)
                        .build())
                .toList();
    }

    private String blankToUnknown(String value) {
        return value == null || value.isBlank() ? "Unassigned" : value;
    }

    private String fullName(Employee employee) {
        if (employee == null) {
            return "Unknown";
        }
        return (employee.getFirstName() + " " + employee.getLastName()).trim();
    }

    private String csvCell(String value) {
        String text = value == null ? "" : value;
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}

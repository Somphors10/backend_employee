package com.kshrd.admsfileservice.employeemanage.service.impl;

import com.kshrd.admsfileservice.employeemanage.exception.DuplicateEmailException;
import com.kshrd.admsfileservice.employeemanage.exception.EmployeeNotFoundException;
import com.kshrd.admsfileservice.employeemanage.exception.InvalidOperationException;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.EmployeeManagerRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.EmployeeRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.EmployeeStatusRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.EmployeeTransferRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.EmployeeHistoryResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.EmployeeResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.EmployeeSummaryResponse;
import com.kshrd.admsfileservice.employeemanage.model.entity.Employee;
import com.kshrd.admsfileservice.employeemanage.model.entity.EmployeeHistory;
import com.kshrd.admsfileservice.employeemanage.model.enums.EmployeeEventType;
import com.kshrd.admsfileservice.employeemanage.model.enums.EmploymentStatus;
import com.kshrd.admsfileservice.employeemanage.repository.EmployeeHistoryRepository;
import com.kshrd.admsfileservice.employeemanage.repository.EmployeeRepository;
import com.kshrd.admsfileservice.employeemanage.repository.LeaveRepository;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeHistoryRepository employeeHistoryRepository;
    private final LeaveRepository leaveRepository;

    public EmployeeServiceImpl(
            EmployeeRepository employeeRepository,
            EmployeeHistoryRepository employeeHistoryRepository,
            LeaveRepository leaveRepository) {
        this.employeeRepository = employeeRepository;
        this.employeeHistoryRepository = employeeHistoryRepository;
        this.leaveRepository = leaveRepository;
    }

    @Override
    public List<EmployeeResponse> getAllEmployees(String department, String query, EmploymentStatus status) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
        return employeeRepository.findAll().stream()
                .filter(employee -> department == null || department.isBlank()
                        || employee.getDepartment().equalsIgnoreCase(department))
                .filter(employee -> status == null || employee.getStatus() == status)
                .filter(employee -> matchesQuery(employee, normalizedQuery))
                .sorted(Comparator.comparing(Employee::getLastName)
                        .thenComparing(Employee::getFirstName))
                .map(EmployeeResponse::from)
                .toList();
    }

    @Override
    public EmployeeResponse getEmployeeById(UUID id) {
        return EmployeeResponse.from(findEmployee(id));
    }

    @Override
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        ensureEmailAvailable(request.getEmail(), null);
        Employee employee = toEmployee(UUID.randomUUID(), request, EmploymentStatus.ACTIVE, null);
        Employee saved = employeeRepository.save(employee);
        recordHistory(saved.getId(), EmployeeEventType.CREATED, "Employee created");
        return EmployeeResponse.from(saved);
    }

    @Override
    public EmployeeResponse updateEmployee(UUID id, EmployeeRequest request) {
        Employee existing = findEmployee(id);
        ensureEmailAvailable(request.getEmail(), id);
        Employee updated = toEmployee(id, request, existing.getStatus(), existing.getManagerId());
        Employee saved = employeeRepository.save(updated);
        recordHistory(id, EmployeeEventType.UPDATED, "Employee profile updated");
        return EmployeeResponse.from(saved);
    }

    @Override
    public EmployeeResponse updateEmployeeStatus(UUID id, EmployeeStatusRequest request) {
        Employee employee = findEmployee(id);
        EmploymentStatus previous = employee.getStatus();
        employee.setStatus(request.getStatus());
        Employee saved = employeeRepository.save(employee);
        recordHistory(id, EmployeeEventType.STATUS_CHANGED,
                "Status changed from " + previous + " to " + request.getStatus());
        return EmployeeResponse.from(saved);
    }

    @Override
    public EmployeeResponse transferEmployee(UUID id, EmployeeTransferRequest request) {
        Employee employee = findEmployee(id);
        String previous = employee.getDepartment() + " / " + employee.getPosition();
        employee.setDepartment(request.getDepartment().trim());
        employee.setPosition(request.getPosition().trim());
        Employee saved = employeeRepository.save(employee);
        recordHistory(id, EmployeeEventType.TRANSFERRED,
                "Transferred from " + previous + " to " + saved.getDepartment() + " / " + saved.getPosition());
        return EmployeeResponse.from(saved);
    }

    @Override
    public EmployeeResponse assignManager(UUID id, EmployeeManagerRequest request) {
        Employee employee = findEmployee(id);
        Employee manager = findEmployee(request.getManagerId());
        if (id.equals(request.getManagerId())) {
            throw new InvalidOperationException("An employee cannot be their own manager");
        }
        if (manager.getStatus() != EmploymentStatus.ACTIVE) {
            throw new InvalidOperationException("Manager must be an active employee");
        }
        ensureNoManagerCycle(id, request.getManagerId());
        employee.setManagerId(request.getManagerId());
        Employee saved = employeeRepository.save(employee);
        recordHistory(id, EmployeeEventType.MANAGER_ASSIGNED, "Assigned manager " + manager.getEmail());
        return EmployeeResponse.from(saved);
    }

    @Override
    public EmployeeResponse clearManager(UUID id) {
        Employee employee = findEmployee(id);
        employee.setManagerId(null);
        Employee saved = employeeRepository.save(employee);
        recordHistory(id, EmployeeEventType.MANAGER_CLEARED, "Manager assignment cleared");
        return EmployeeResponse.from(saved);
    }

    @Override
    public List<EmployeeResponse> getSubordinates(UUID id) {
        findEmployee(id);
        return employeeRepository.findByManagerId(id).stream()
                .sorted(Comparator.comparing(Employee::getLastName)
                        .thenComparing(Employee::getFirstName))
                .map(EmployeeResponse::from)
                .toList();
    }

    @Override
    public List<EmployeeHistoryResponse> getEmployeeHistory(UUID id) {
        findEmployee(id);
        return employeeHistoryRepository.findByEmployeeIdOrderByOccurredAtDesc(id).stream()
                .map(EmployeeHistoryResponse::from)
                .toList();
    }

    @Override
    public EmployeeSummaryResponse getEmployeeSummary() {
        List<Employee> employees = employeeRepository.findAll();
        long activeEmployees = employees.stream()
                .filter(employee -> employee.getStatus() == EmploymentStatus.ACTIVE)
                .count();

        List<EmployeeSummaryResponse.DepartmentSummary> byDepartment = employees.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment, TreeMap::new, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> EmployeeSummaryResponse.DepartmentSummary.builder()
                        .department(entry.getKey())
                        .employeeCount(entry.getValue())
                        .build())
                .toList();

        return EmployeeSummaryResponse.builder()
                .totalEmployees(employees.size())
                .activeEmployees(activeEmployees)
                .inactiveEmployees(employees.size() - activeEmployees)
                .byDepartment(byDepartment)
                .build();
    }

    @Override
    public List<String> getDepartments() {
        return employeeRepository.findAll().stream()
                .map(Employee::getDepartment)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    @Override
    public List<String> getPositions() {
        return employeeRepository.findAll().stream()
                .map(Employee::getPosition)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    @Override
    public void deleteEmployee(UUID id) {
        findEmployee(id);
        employeeRepository.findByManagerId(id).forEach(subordinate -> {
            subordinate.setManagerId(null);
            employeeRepository.save(subordinate);
        });
        leaveRepository.deleteByEmployeeId(id);
        recordHistory(id, EmployeeEventType.DELETED, "Employee deleted");
        employeeRepository.deleteById(id);
    }

    private Employee findEmployee(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    private void ensureEmailAvailable(String email, UUID currentEmployeeId) {
        employeeRepository.findByEmailIgnoreCase(email)
                .filter(existing -> currentEmployeeId == null || !existing.getId().equals(currentEmployeeId))
                .ifPresent(existing -> {
                    throw new DuplicateEmailException(email);
                });
    }

    private void ensureNoManagerCycle(UUID employeeId, UUID managerId) {
        UUID current = managerId;
        while (current != null) {
            if (current.equals(employeeId)) {
                throw new InvalidOperationException("Manager assignment would create a reporting cycle");
            }
            current = findEmployee(current).getManagerId();
        }
    }

    private boolean matchesQuery(Employee employee, String query) {
        if (query.isBlank()) {
            return true;
        }
        return contains(employee.getFirstName(), query)
                || contains(employee.getLastName(), query)
                || contains(employee.getEmail(), query)
                || contains(employee.getPosition(), query);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private void recordHistory(UUID employeeId, EmployeeEventType eventType, String description) {
        employeeHistoryRepository.save(EmployeeHistory.builder()
                .id(UUID.randomUUID())
                .employeeId(employeeId)
                .eventType(eventType)
                .description(description)
                .occurredAt(Instant.now())
                .build());
    }

    private Employee toEmployee(UUID id, EmployeeRequest request, EmploymentStatus status, UUID managerId) {
        return Employee.builder()
                .id(id)
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .phoneNumber(request.getPhoneNumber().trim())
                .position(request.getPosition().trim())
                .department(request.getDepartment().trim())
                .hireDate(request.getHireDate())
                .status(status == null ? EmploymentStatus.ACTIVE : status)
                .managerId(managerId)
                .build();
    }
}

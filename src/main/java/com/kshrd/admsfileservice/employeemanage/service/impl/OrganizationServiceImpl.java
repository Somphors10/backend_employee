package com.kshrd.admsfileservice.employeemanage.service.impl;

import com.kshrd.admsfileservice.employeemanage.exception.InvalidOperationException;
import com.kshrd.admsfileservice.employeemanage.exception.ResourceNotFoundException;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.OrganizationDepartmentRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.OrganizationDepartmentResponse;
import com.kshrd.admsfileservice.employeemanage.model.entity.Employee;
import com.kshrd.admsfileservice.employeemanage.model.entity.OrganizationDepartment;
import com.kshrd.admsfileservice.employeemanage.repository.EmployeeRepository;
import com.kshrd.admsfileservice.employeemanage.repository.OrganizationDepartmentRepository;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import com.kshrd.admsfileservice.employeemanage.service.OrganizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrganizationServiceImpl implements OrganizationService {
    private final OrganizationDepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;

    public OrganizationServiceImpl(
            OrganizationDepartmentRepository departmentRepository,
            EmployeeRepository employeeRepository,
            EmployeeService employeeService) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.employeeService = employeeService;
    }

    @Override
    public List<OrganizationDepartmentResponse> getDepartments() {
        Map<String, Long> counts = employeeCounts();
        return departmentRepository.findAll().stream()
                .sorted(Comparator.comparing(OrganizationDepartment::getName, String.CASE_INSENSITIVE_ORDER))
                .map(department -> OrganizationDepartmentResponse.from(department, countFor(counts, department.getName())))
                .toList();
    }

    @Override
    public OrganizationDepartmentResponse getDepartmentById(UUID id) {
        OrganizationDepartment department = findDepartment(id);
        return OrganizationDepartmentResponse.from(department, employeesIn(department.getName()).size());
    }

    @Override
    public OrganizationDepartmentResponse createDepartment(OrganizationDepartmentRequest request) {
        ensureNameAvailable(request.getName(), null);
        if (request.getManagerId() != null) {
            employeeService.getEmployeeById(request.getManagerId());
        }
        OrganizationDepartment department = OrganizationDepartment.builder()
                .id(UUID.randomUUID())
                .name(request.getName().trim())
                .description(trimToNull(request.getDescription()))
                .managerId(request.getManagerId())
                .build();
        OrganizationDepartment saved = departmentRepository.save(department);
        return OrganizationDepartmentResponse.from(saved, employeesIn(saved.getName()).size());
    }

    @Override
    public OrganizationDepartmentResponse updateDepartment(UUID id, OrganizationDepartmentRequest request) {
        OrganizationDepartment department = findDepartment(id);
        ensureNameAvailable(request.getName(), id);
        if (request.getManagerId() != null) {
            employeeService.getEmployeeById(request.getManagerId());
        }
        String oldName = department.getName();
        String newName = request.getName().trim();
        department.setName(newName);
        department.setDescription(trimToNull(request.getDescription()));
        department.setManagerId(request.getManagerId());
        OrganizationDepartment saved = departmentRepository.save(department);
        if (!oldName.equalsIgnoreCase(newName)) {
            employeesIn(oldName).forEach(employee -> {
                employee.setDepartment(newName);
                employeeRepository.save(employee);
            });
        }
        return OrganizationDepartmentResponse.from(saved, employeesIn(saved.getName()).size());
    }

    @Override
    public void deleteDepartment(UUID id) {
        OrganizationDepartment department = findDepartment(id);
        long assigned = employeesIn(department.getName()).size();
        if (assigned > 0) {
            throw new InvalidOperationException(
                    "Cannot delete this department while " + assigned + " employee(s) are assigned to it");
        }
        departmentRepository.delete(department);
    }

    private OrganizationDepartment findDepartment(UUID id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
    }

    private void ensureNameAvailable(String name, UUID currentId) {
        departmentRepository.findByNameIgnoreCase(name.trim())
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new InvalidOperationException("Department already exists: " + name);
                });
    }

    private List<Employee> employeesIn(String name) {
        if (name == null || name.isBlank()) {
            return List.of();
        }
        return employeeRepository.findAll().stream()
                .filter(employee -> employee.getDepartment() != null
                        && employee.getDepartment().equalsIgnoreCase(name))
                .toList();
    }

    private Map<String, Long> employeeCounts() {
        return employeeRepository.findAll().stream()
                .filter(employee -> employee.getDepartment() != null && !employee.getDepartment().isBlank())
                .collect(Collectors.groupingBy(employee -> employee.getDepartment().toLowerCase(), Collectors.counting()));
    }

    private long countFor(Map<String, Long> counts, String name) {
        if (name == null) {
            return 0;
        }
        return counts.getOrDefault(name.toLowerCase(), 0L);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}

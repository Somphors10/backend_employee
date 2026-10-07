package com.kshrd.admsfileservice.employeemanage.service.impl;

import com.kshrd.admsfileservice.employeemanage.exception.InvalidOperationException;
import com.kshrd.admsfileservice.employeemanage.exception.ResourceNotFoundException;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.OrganizationDepartmentRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.OrganizationDepartmentResponse;
import com.kshrd.admsfileservice.employeemanage.model.entity.OrganizationDepartment;
import com.kshrd.admsfileservice.employeemanage.repository.OrganizationDepartmentRepository;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import com.kshrd.admsfileservice.employeemanage.service.OrganizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrganizationServiceImpl implements OrganizationService {
    private final OrganizationDepartmentRepository departmentRepository;
    private final EmployeeService employeeService;

    public OrganizationServiceImpl(
            OrganizationDepartmentRepository departmentRepository,
            EmployeeService employeeService) {
        this.departmentRepository = departmentRepository;
        this.employeeService = employeeService;
    }

    @Override
    public List<OrganizationDepartmentResponse> getDepartments() {
        return departmentRepository.findAll().stream()
                .sorted(Comparator.comparing(OrganizationDepartment::getName, String.CASE_INSENSITIVE_ORDER))
                .map(OrganizationDepartmentResponse::from)
                .toList();
    }

    @Override
    public OrganizationDepartmentResponse getDepartmentById(UUID id) {
        return OrganizationDepartmentResponse.from(findDepartment(id));
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
        return OrganizationDepartmentResponse.from(departmentRepository.save(department));
    }

    @Override
    public OrganizationDepartmentResponse updateDepartment(UUID id, OrganizationDepartmentRequest request) {
        OrganizationDepartment department = findDepartment(id);
        ensureNameAvailable(request.getName(), id);
        if (request.getManagerId() != null) {
            employeeService.getEmployeeById(request.getManagerId());
        }
        department.setName(request.getName().trim());
        department.setDescription(trimToNull(request.getDescription()));
        department.setManagerId(request.getManagerId());
        return OrganizationDepartmentResponse.from(departmentRepository.save(department));
    }

    @Override
    public void deleteDepartment(UUID id) {
        departmentRepository.delete(findDepartment(id));
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

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}

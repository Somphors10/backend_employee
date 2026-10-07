package com.kshrd.admsfileservice.employeemanage.service;

import com.kshrd.admsfileservice.employeemanage.model.dto.request.EmployeeManagerRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.EmployeeRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.EmployeeStatusRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.EmployeeTransferRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.EmployeeHistoryResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.EmployeeResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.EmployeeSummaryResponse;
import com.kshrd.admsfileservice.employeemanage.model.enums.EmploymentStatus;

import java.util.List;
import java.util.UUID;

public interface EmployeeService {
    List<EmployeeResponse> getAllEmployees(String department, String query, EmploymentStatus status);

    EmployeeResponse getEmployeeById(UUID id);

    EmployeeResponse createEmployee(EmployeeRequest request);

    EmployeeResponse updateEmployee(UUID id, EmployeeRequest request);

    EmployeeResponse updateEmployeeStatus(UUID id, EmployeeStatusRequest request);

    EmployeeResponse transferEmployee(UUID id, EmployeeTransferRequest request);

    EmployeeResponse assignManager(UUID id, EmployeeManagerRequest request);

    EmployeeResponse clearManager(UUID id);

    List<EmployeeResponse> getSubordinates(UUID id);

    List<EmployeeHistoryResponse> getEmployeeHistory(UUID id);

    EmployeeSummaryResponse getEmployeeSummary();

    List<String> getDepartments();

    List<String> getPositions();

    void deleteEmployee(UUID id);
}

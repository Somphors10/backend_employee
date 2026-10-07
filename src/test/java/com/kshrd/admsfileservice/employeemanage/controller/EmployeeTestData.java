package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.model.dto.request.EmployeeRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.EmployeeResponse;
import com.kshrd.admsfileservice.employeemanage.model.enums.EmploymentStatus;

import java.time.LocalDate;
import java.util.UUID;

final class EmployeeTestData {
    static final UUID EMPLOYEE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private EmployeeTestData() {
    }

    static EmployeeRequest request() {
        return EmployeeRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .phoneNumber("012345678")
                .position("Software Engineer")
                .department("IT")
                .hireDate(LocalDate.of(2024, 1, 15))
                .build();
    }

    static EmployeeResponse response() {
        return EmployeeResponse.builder()
                .id(EMPLOYEE_ID)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .phoneNumber("012345678")
                .position("Software Engineer")
                .department("IT")
                .hireDate(LocalDate.of(2024, 1, 15))
                .status(EmploymentStatus.ACTIVE)
                .build();
    }
}

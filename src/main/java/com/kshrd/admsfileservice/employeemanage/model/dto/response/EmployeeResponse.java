package com.kshrd.admsfileservice.employeemanage.model.dto.response;

import com.kshrd.admsfileservice.employeemanage.model.entity.Employee;
import com.kshrd.admsfileservice.employeemanage.model.enums.EmploymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String position;
    private String department;
    private LocalDate hireDate;
    private EmploymentStatus status;
    private UUID managerId;
    private String nationalId;
    private LocalDate dateOfBirth;
    private String address;
    private BigDecimal salary;
    private boolean hasPhoto;
    private String photoUrl;

    public static EmployeeResponse from(Employee employee) {
        EmploymentStatus status = employee.getStatus() == null
                ? EmploymentStatus.ACTIVE
                : employee.getStatus();
        return EmployeeResponse.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .phoneNumber(employee.getPhoneNumber())
                .position(employee.getPosition())
                .department(employee.getDepartment())
                .hireDate(employee.getHireDate())
                .status(status)
                .managerId(employee.getManagerId())
                .nationalId(employee.getNationalId())
                .dateOfBirth(employee.getDateOfBirth())
                .address(employee.getAddress())
                .salary(employee.getSalary())
                .hasPhoto(employee.getPhotoFileName() != null && !employee.getPhotoFileName().isBlank())
                .photoUrl(employee.getPhotoFileName() == null || employee.getPhotoFileName().isBlank()
                        ? null
                        : "/api/v1/employees/" + employee.getId() + "/photo")
                .build();
    }
}

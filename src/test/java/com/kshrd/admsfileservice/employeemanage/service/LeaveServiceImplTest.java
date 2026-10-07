package com.kshrd.admsfileservice.employeemanage.service;

import com.kshrd.admsfileservice.employeemanage.exception.InvalidOperationException;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.EmployeeRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.LeaveRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.LeaveResponse;
import com.kshrd.admsfileservice.employeemanage.model.enums.LeaveStatus;
import com.kshrd.admsfileservice.employeemanage.model.enums.LeaveType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class LeaveServiceImplTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private LeaveService leaveService;

    @Test
    void createAndApproveLeave() {
        var employee = employeeService.createEmployee(sampleEmployee());
        LeaveResponse created = leaveService.createLeave(sampleLeave(employee.getId()));

        assertEquals(LeaveStatus.PENDING, created.getStatus());

        LeaveResponse approved = leaveService.approveLeave(created.getId());

        assertEquals(LeaveStatus.APPROVED, approved.getStatus());
        assertNotNull(approved.getDecidedAt());
        assertEquals(1, leaveService.getLeaves(employee.getId(), LeaveStatus.APPROVED).size());
    }

    @Test
    void rejectLeaveMarksRequestRejected() {
        var employee = employeeService.createEmployee(sampleEmployee());
        LeaveResponse created = leaveService.createLeave(sampleLeave(employee.getId()));

        LeaveResponse rejected = leaveService.rejectLeave(created.getId());

        assertEquals(LeaveStatus.REJECTED, rejected.getStatus());
    }

    @Test
    void cannotDecideLeaveTwice() {
        var employee = employeeService.createEmployee(sampleEmployee());
        LeaveResponse created = leaveService.createLeave(sampleLeave(employee.getId()));
        leaveService.approveLeave(created.getId());

        assertThrows(InvalidOperationException.class, () -> leaveService.rejectLeave(created.getId()));
    }

    @Test
    void getLeavesCanFilterByEmployee() {
        var jane = employeeService.createEmployee(sampleEmployee());
        var john = employeeService.createEmployee(EmployeeRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phoneNumber("012345678")
                .position("Recruiter")
                .department("HR")
                .hireDate(LocalDate.of(2024, 1, 15))
                .build());
        leaveService.createLeave(sampleLeave(jane.getId()));
        leaveService.createLeave(sampleLeave(john.getId()));

        List<LeaveResponse> janeLeaves = leaveService.getLeaves(jane.getId(), null);

        assertEquals(1, janeLeaves.size());
        assertEquals(jane.getId(), janeLeaves.getFirst().getEmployeeId());
    }

    private EmployeeRequest sampleEmployee() {
        return EmployeeRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .phoneNumber("012345678")
                .position("Software Engineer")
                .department("IT")
                .hireDate(LocalDate.of(2024, 1, 15))
                .build();
    }

    private LeaveRequest sampleLeave(java.util.UUID employeeId) {
        return LeaveRequest.builder()
                .employeeId(employeeId)
                .type(LeaveType.ANNUAL)
                .startDate(LocalDate.of(2026, 10, 10))
                .endDate(LocalDate.of(2026, 10, 12))
                .reason("Family trip")
                .build();
    }
}

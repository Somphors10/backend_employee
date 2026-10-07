package com.kshrd.admsfileservice.employeemanage.service;

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
import com.kshrd.admsfileservice.employeemanage.model.enums.EmployeeEventType;
import com.kshrd.admsfileservice.employeemanage.model.enums.EmploymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class EmployeeServiceImplTest {

    @Autowired
    private EmployeeService employeeService;

    @Test
    void createAndGetEmployee() {
        EmployeeResponse created = employeeService.createEmployee(sampleRequest("Jane", "IT", "jane@example.com"));

        EmployeeResponse found = employeeService.getEmployeeById(created.getId());

        assertEquals("Jane", found.getFirstName());
        assertEquals("jane@example.com", found.getEmail());
        assertEquals("IT", found.getDepartment());
    }

    @Test
    void getAllEmployeesFiltersByDepartment() {
        employeeService.createEmployee(sampleRequest("Jane", "IT", "jane@example.com"));
        employeeService.createEmployee(sampleRequest("John", "HR", "john@example.com"));

        List<EmployeeResponse> itEmployees = employeeService.getAllEmployees("IT", null, null);

        assertEquals(1, itEmployees.size());
        assertEquals("Jane", itEmployees.getFirst().getFirstName());
    }

    @Test
    void createEmployeeRejectsDuplicateEmail() {
        employeeService.createEmployee(sampleRequest("Jane", "IT", "jane@example.com"));

        assertThrows(DuplicateEmailException.class,
                () -> employeeService.createEmployee(sampleRequest("Janet", "HR", "jane@example.com")));
    }

    @Test
    void updateEmployeeChangesFields() {
        EmployeeResponse created = employeeService.createEmployee(sampleRequest("Jane", "IT", "jane@example.com"));
        EmployeeRequest update = sampleRequest("Janet", "HR", "janet@example.com");

        EmployeeResponse updated = employeeService.updateEmployee(created.getId(), update);

        assertEquals("Janet", updated.getFirstName());
        assertEquals("HR", updated.getDepartment());
        assertEquals("janet@example.com", updated.getEmail());
    }

    @Test
    void deleteEmployeeRemovesRecord() {
        EmployeeResponse created = employeeService.createEmployee(sampleRequest("Jane", "IT", "jane@example.com"));

        employeeService.deleteEmployee(created.getId());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(created.getId()));
        assertTrue(employeeService.getAllEmployees(null, null, null).isEmpty());
    }

    @Test
    void getEmployeeByIdThrowsWhenMissing() {
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(UUID.randomUUID()));
    }

    @Test
    void searchEmployeesMatchesNameAndEmail() {
        employeeService.createEmployee(sampleRequest("Jane", "IT", "jane@example.com"));
        employeeService.createEmployee(sampleRequest("John", "HR", "john@example.com"));

        List<EmployeeResponse> byName = employeeService.getAllEmployees(null, "jane", null);
        List<EmployeeResponse> byEmail = employeeService.getAllEmployees(null, "john@", null);

        assertEquals(1, byName.size());
        assertEquals("Jane", byName.getFirst().getFirstName());
        assertEquals(1, byEmail.size());
        assertEquals("John", byEmail.getFirst().getFirstName());
    }

    @Test
    void updateEmployeeStatusDeactivatesEmployee() {
        EmployeeResponse created = employeeService.createEmployee(sampleRequest("Jane", "IT", "jane@example.com"));

        EmployeeResponse updated = employeeService.updateEmployeeStatus(
                created.getId(),
                EmployeeStatusRequest.builder().status(EmploymentStatus.INACTIVE).build());

        assertEquals(EmploymentStatus.INACTIVE, updated.getStatus());
        assertEquals(1, employeeService.getAllEmployees(null, null, EmploymentStatus.INACTIVE).size());
        assertTrue(employeeService.getAllEmployees(null, null, EmploymentStatus.ACTIVE).isEmpty());
    }

    @Test
    void transferEmployeeChangesDepartmentAndPosition() {
        EmployeeResponse created = employeeService.createEmployee(sampleRequest("Jane", "IT", "jane@example.com"));

        EmployeeResponse transferred = employeeService.transferEmployee(
                created.getId(),
                EmployeeTransferRequest.builder().department("HR").position("HR Specialist").build());

        assertEquals("HR", transferred.getDepartment());
        assertEquals("HR Specialist", transferred.getPosition());
        assertEquals(EmploymentStatus.ACTIVE, transferred.getStatus());
    }

    @Test
    void updateEmployeeKeepsExistingStatus() {
        EmployeeResponse created = employeeService.createEmployee(sampleRequest("Jane", "IT", "jane@example.com"));
        employeeService.updateEmployeeStatus(
                created.getId(),
                EmployeeStatusRequest.builder().status(EmploymentStatus.INACTIVE).build());

        EmployeeResponse updated = employeeService.updateEmployee(
                created.getId(),
                sampleRequest("Janet", "IT", "janet@example.com"));

        assertEquals("Janet", updated.getFirstName());
        assertEquals(EmploymentStatus.INACTIVE, updated.getStatus());
    }

    @Test
    void getEmployeeSummaryAndDepartments() {
        employeeService.createEmployee(sampleRequest("Jane", "IT", "jane@example.com"));
        employeeService.createEmployee(sampleRequest("John", "HR", "john@example.com"));
        EmployeeResponse inactive = employeeService.createEmployee(sampleRequest("Alex", "IT", "alex@example.com"));
        employeeService.updateEmployeeStatus(
                inactive.getId(),
                EmployeeStatusRequest.builder().status(EmploymentStatus.INACTIVE).build());

        EmployeeSummaryResponse summary = employeeService.getEmployeeSummary();
        List<String> departments = employeeService.getDepartments();

        assertEquals(3, summary.getTotalEmployees());
        assertEquals(2, summary.getActiveEmployees());
        assertEquals(1, summary.getInactiveEmployees());
        assertEquals(2, summary.getByDepartment().size());
        assertEquals(List.of("HR", "IT"), departments);
        assertEquals(List.of("Software Engineer"), employeeService.getPositions());
    }

    @Test
    void assignManagerAndListSubordinates() {
        EmployeeResponse manager = employeeService.createEmployee(sampleRequest("John", "IT", "john@example.com"));
        EmployeeResponse employee = employeeService.createEmployee(sampleRequest("Jane", "IT", "jane@example.com"));

        EmployeeResponse updated = employeeService.assignManager(
                employee.getId(),
                EmployeeManagerRequest.builder().managerId(manager.getId()).build());

        assertEquals(manager.getId(), updated.getManagerId());
        assertEquals(1, employeeService.getSubordinates(manager.getId()).size());
        assertEquals("Jane", employeeService.getSubordinates(manager.getId()).getFirst().getFirstName());
    }

    @Test
    void assignManagerRejectsSelfAssignment() {
        EmployeeResponse employee = employeeService.createEmployee(sampleRequest("Jane", "IT", "jane@example.com"));

        assertThrows(InvalidOperationException.class, () -> employeeService.assignManager(
                employee.getId(),
                EmployeeManagerRequest.builder().managerId(employee.getId()).build()));
    }

    @Test
    void employeeHistoryRecordsLifecycleEvents() {
        EmployeeResponse created = employeeService.createEmployee(sampleRequest("Jane", "IT", "jane@example.com"));
        employeeService.transferEmployee(
                created.getId(),
                EmployeeTransferRequest.builder().department("HR").position("Recruiter").build());

        List<EmployeeHistoryResponse> history = employeeService.getEmployeeHistory(created.getId());

        assertEquals(2, history.size());
        assertTrue(history.stream().anyMatch(event -> event.getEventType() == EmployeeEventType.CREATED));
        assertTrue(history.stream().anyMatch(event -> event.getEventType() == EmployeeEventType.TRANSFERRED));
    }

    private EmployeeRequest sampleRequest(String firstName, String department, String email) {
        return EmployeeRequest.builder()
                .firstName(firstName)
                .lastName("Doe")
                .email(email)
                .phoneNumber("012345678")
                .position("Software Engineer")
                .department(department)
                .hireDate(LocalDate.of(2024, 1, 15))
                .build();
    }
}

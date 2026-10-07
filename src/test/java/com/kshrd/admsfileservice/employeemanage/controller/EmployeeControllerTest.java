package com.kshrd.admsfileservice.employeemanage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kshrd.admsfileservice.employeemanage.exception.DuplicateEmailException;
import com.kshrd.admsfileservice.employeemanage.exception.EmployeeNotFoundException;
import com.kshrd.admsfileservice.employeemanage.exception.GlobalExceptionHandler;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.EmployeeRequest;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.kshrd.admsfileservice.employeemanage.controller.EmployeeTestData.EMPLOYEE_ID;
import static com.kshrd.admsfileservice.employeemanage.controller.EmployeeTestData.request;
import static com.kshrd.admsfileservice.employeemanage.controller.EmployeeTestData.response;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    void getAllEmployeesReturnsList() throws Exception {
        when(employeeService.getAllEmployees(null, null, null)).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Employees retrieved successfully"))
                .andExpect(jsonPath("$.payload[0].email").value("jane.doe@example.com"));
    }

    @Test
    void getEmployeeByIdReturnsEmployee() throws Exception {
        when(employeeService.getEmployeeById(EMPLOYEE_ID)).thenReturn(response());

        mockMvc.perform(get("/api/v1/employees/{id}", EMPLOYEE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.id").value(EMPLOYEE_ID.toString()))
                .andExpect(jsonPath("$.payload.firstName").value("Jane"));
    }

    @Test
    void getEmployeeByIdReturnsNotFound() throws Exception {
        when(employeeService.getEmployeeById(EMPLOYEE_ID)).thenThrow(new EmployeeNotFoundException(EMPLOYEE_ID));

        mockMvc.perform(get("/api/v1/employees/{id}", EMPLOYEE_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found with id: " + EMPLOYEE_ID));
    }

    @Test
    void createEmployeeReturnsCreated() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Employee created successfully"))
                .andExpect(jsonPath("$.payload.email").value("jane.doe@example.com"));
    }

    @Test
    void createEmployeeReturnsBadRequestWhenInvalid() throws Exception {
        EmployeeRequest invalid = request();
        invalid.setEmail("not-an-email");

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEmployeeReturnsConflictWhenEmailExists() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequest.class)))
                .thenThrow(new DuplicateEmailException("jane.doe@example.com"));

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isConflict());
    }

    @Test
    void updateEmployeeReturnsUpdated() throws Exception {
        when(employeeService.updateEmployee(eq(EMPLOYEE_ID), any(EmployeeRequest.class)))
                .thenReturn(response());

        mockMvc.perform(put("/api/v1/employees/{id}", EMPLOYEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Employee updated successfully"));
    }

    @Test
    void deleteEmployeeReturnsOk() throws Exception {
        doNothing().when(employeeService).deleteEmployee(EMPLOYEE_ID);

        mockMvc.perform(delete("/api/v1/employees/{id}", EMPLOYEE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Employee deleted successfully"));
    }

    @Test
    void deleteEmployeeReturnsNotFound() throws Exception {
        doThrow(new EmployeeNotFoundException(EMPLOYEE_ID)).when(employeeService).deleteEmployee(EMPLOYEE_ID);

        mockMvc.perform(delete("/api/v1/employees/{id}", EMPLOYEE_ID))
                .andExpect(status().isNotFound());
    }
}

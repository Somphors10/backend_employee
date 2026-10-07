package com.kshrd.admsfileservice.employeemanage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kshrd.admsfileservice.employeemanage.exception.GlobalExceptionHandler;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.EmployeeStatusRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.EmployeeResponse;
import com.kshrd.admsfileservice.employeemanage.model.enums.EmploymentStatus;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.kshrd.admsfileservice.employeemanage.controller.EmployeeTestData.EMPLOYEE_ID;
import static com.kshrd.admsfileservice.employeemanage.controller.EmployeeTestData.response;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeStatusController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class EmployeeStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    void updateEmployeeStatusReturnsUpdated() throws Exception {
        EmployeeResponse inactive = response();
        inactive.setStatus(EmploymentStatus.INACTIVE);
        when(employeeService.updateEmployeeStatus(eq(EMPLOYEE_ID), any(EmployeeStatusRequest.class)))
                .thenReturn(inactive);

        mockMvc.perform(patch("/api/v1/employees/{id}/status", EMPLOYEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                EmployeeStatusRequest.builder().status(EmploymentStatus.INACTIVE).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Employee status updated successfully"))
                .andExpect(jsonPath("$.payload.status").value("INACTIVE"));
    }
}

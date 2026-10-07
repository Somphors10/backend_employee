package com.kshrd.admsfileservice.employeemanage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kshrd.admsfileservice.employeemanage.exception.GlobalExceptionHandler;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.EmployeeTransferRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.EmployeeResponse;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

@WebMvcTest(EmployeeTransferController.class)
@Import(GlobalExceptionHandler.class)
class EmployeeTransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    void transferEmployeeReturnsUpdated() throws Exception {
        EmployeeResponse transferred = response();
        transferred.setDepartment("HR");
        transferred.setPosition("HR Specialist");
        when(employeeService.transferEmployee(eq(EMPLOYEE_ID), any(EmployeeTransferRequest.class)))
                .thenReturn(transferred);

        mockMvc.perform(patch("/api/v1/employees/{id}/transfer", EMPLOYEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                EmployeeTransferRequest.builder()
                                        .department("HR")
                                        .position("HR Specialist")
                                        .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Employee transferred successfully"))
                .andExpect(jsonPath("$.payload.department").value("HR"));
    }
}

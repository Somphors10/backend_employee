package com.kshrd.admsfileservice.employeemanage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kshrd.admsfileservice.employeemanage.exception.GlobalExceptionHandler;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.EmployeeManagerRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.EmployeeResponse;
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
import java.util.UUID;

import static com.kshrd.admsfileservice.employeemanage.controller.EmployeeTestData.EMPLOYEE_ID;
import static com.kshrd.admsfileservice.employeemanage.controller.EmployeeTestData.response;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeManagerController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class EmployeeManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private EmployeeService employeeService;

    private final UUID managerId = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Test
    void assignManagerReturnsUpdated() throws Exception {
        EmployeeResponse assigned = response();
        assigned.setManagerId(managerId);
        when(employeeService.assignManager(eq(EMPLOYEE_ID), any(EmployeeManagerRequest.class)))
                .thenReturn(assigned);

        mockMvc.perform(patch("/api/v1/employees/{id}/manager", EMPLOYEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                EmployeeManagerRequest.builder().managerId(managerId).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Manager assigned successfully"))
                .andExpect(jsonPath("$.payload.managerId").value(managerId.toString()));
    }

    @Test
    void getSubordinatesReturnsList() throws Exception {
        when(employeeService.getSubordinates(managerId)).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/v1/employees/{id}/subordinates", managerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload[0].firstName").value("Jane"));
    }

    @Test
    void clearManagerReturnsUpdated() throws Exception {
        when(employeeService.clearManager(EMPLOYEE_ID)).thenReturn(response());

        mockMvc.perform(delete("/api/v1/employees/{id}/manager", EMPLOYEE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Manager cleared successfully"));
    }
}

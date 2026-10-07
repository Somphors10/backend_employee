package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.exception.GlobalExceptionHandler;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.EmployeeHistoryResponse;
import com.kshrd.admsfileservice.employeemanage.model.enums.EmployeeEventType;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.kshrd.admsfileservice.employeemanage.controller.EmployeeTestData.EMPLOYEE_ID;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeHistoryController.class)
@Import(GlobalExceptionHandler.class)
class EmployeeHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    void getEmployeeHistoryReturnsEvents() throws Exception {
        when(employeeService.getEmployeeHistory(EMPLOYEE_ID)).thenReturn(List.of(
                EmployeeHistoryResponse.builder()
                        .id(UUID.randomUUID())
                        .employeeId(EMPLOYEE_ID)
                        .eventType(EmployeeEventType.CREATED)
                        .description("Employee created")
                        .occurredAt(Instant.parse("2026-10-06T09:00:00Z"))
                        .build()));

        mockMvc.perform(get("/api/v1/employees/{id}/history", EMPLOYEE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload[0].eventType").value("CREATED"));
    }
}

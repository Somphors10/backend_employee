package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.exception.GlobalExceptionHandler;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.EmployeeSummaryResponse;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeSummaryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class EmployeeSummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    void getEmployeeSummaryReturnsTotals() throws Exception {
        EmployeeSummaryResponse summary = EmployeeSummaryResponse.builder()
                .totalEmployees(2)
                .activeEmployees(1)
                .inactiveEmployees(1)
                .byDepartment(List.of(
                        EmployeeSummaryResponse.DepartmentSummary.builder()
                                .department("IT")
                                .employeeCount(2)
                                .build()))
                .build();
        when(employeeService.getEmployeeSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/v1/employees/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.totalEmployees").value(2))
                .andExpect(jsonPath("$.payload.activeEmployees").value(1));
    }
}

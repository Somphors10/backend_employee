package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.exception.GlobalExceptionHandler;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.DashboardResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.NavigationItemResponse;
import com.kshrd.admsfileservice.employeemanage.service.DashboardService;
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

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    void getDashboardReturnsTotals() throws Exception {
        when(dashboardService.getDashboard()).thenReturn(DashboardResponse.builder()
                .totalEmployees(5)
                .activeEmployees(4)
                .pendingLeaves(2)
                .todayAttendance(3)
                .pendingPayrolls(1)
                .build());

        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.totalEmployees").value(5));
    }

    @Test
    void getNavigationReturnsSidebarItems() throws Exception {
        when(dashboardService.getNavigation()).thenReturn(List.of(
                NavigationItemResponse.builder()
                        .key("employees")
                        .label("Employees")
                        .path("/employees")
                        .status("LIVE")
                        .build()));

        mockMvc.perform(get("/api/v1/navigation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload[0].key").value("employees"));
    }
}

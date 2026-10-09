package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.exception.GlobalExceptionHandler;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.ReportOverviewResponse;
import com.kshrd.admsfileservice.employeemanage.service.ReportService;
import com.kshrd.admsfileservice.employeemanage.service.StoredDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @Test
    void summaryReturnsOverview() throws Exception {
        when(reportService.getOverview(any(), any())).thenReturn(ReportOverviewResponse.builder()
                .from(LocalDate.of(2026, 1, 1))
                .to(LocalDate.of(2026, 10, 9))
                .employees(10)
                .pendingLeaves(2)
                .todayAttendance(4)
                .pendingPayrolls(1)
                .pendingOvertimes(3)
                .people(ReportOverviewResponse.PeopleReport.builder()
                        .total(10)
                        .active(9)
                        .inactive(1)
                        .newHires(2)
                        .build())
                .build());

        mockMvc.perform(get("/api/v1/reports/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.employees").value(10))
                .andExpect(jsonPath("$.payload.people.active").value(9));
    }

    @Test
    void exportReturnsCsv() throws Exception {
        byte[] csv = "Name,Email\nSokha Chan,sokha@company.com\n".getBytes();
        when(reportService.exportCsv(eq("people"), any(), any())).thenReturn(new StoredDocument(
                new ByteArrayResource(csv),
                "hr-people.csv",
                "text/csv",
                csv.length));

        mockMvc.perform(get("/api/v1/reports/export").param("type", "people"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("hr-people.csv")));
    }
}

package com.kshrd.admsfileservice.employeemanage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kshrd.admsfileservice.employeemanage.exception.GlobalExceptionHandler;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.AttendanceCheckRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.AttendanceResponse;
import com.kshrd.admsfileservice.employeemanage.model.enums.AttendanceStatus;
import com.kshrd.admsfileservice.employeemanage.service.AttendanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static com.kshrd.admsfileservice.employeemanage.controller.EmployeeTestData.EMPLOYEE_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttendanceController.class)
@Import(GlobalExceptionHandler.class)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private AttendanceService attendanceService;

    @Test
    void checkInReturnsCreated() throws Exception {
        when(attendanceService.checkIn(any(AttendanceCheckRequest.class))).thenReturn(AttendanceResponse.builder()
                .id(UUID.randomUUID())
                .employeeId(EMPLOYEE_ID)
                .workDate(LocalDate.now())
                .checkIn(LocalTime.of(8, 30))
                .status(AttendanceStatus.PRESENT)
                .build());

        mockMvc.perform(post("/api/v1/attendances/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                AttendanceCheckRequest.builder().employeeId(EMPLOYEE_ID).build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.payload.status").value("PRESENT"));
    }
}

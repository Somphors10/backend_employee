package com.kshrd.admsfileservice.employeemanage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kshrd.admsfileservice.employeemanage.exception.GlobalExceptionHandler;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.LeaveRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.LeaveResponse;
import com.kshrd.admsfileservice.employeemanage.model.enums.LeaveStatus;
import com.kshrd.admsfileservice.employeemanage.model.enums.LeaveType;
import com.kshrd.admsfileservice.employeemanage.service.LeaveService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.kshrd.admsfileservice.employeemanage.controller.EmployeeTestData.EMPLOYEE_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LeaveController.class)
@Import(GlobalExceptionHandler.class)
class LeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private LeaveService leaveService;

    private final UUID leaveId = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Test
    void createLeaveReturnsCreated() throws Exception {
        when(leaveService.createLeave(any(LeaveRequest.class))).thenReturn(sampleLeave());

        mockMvc.perform(post("/api/v1/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Leave request created successfully"))
                .andExpect(jsonPath("$.payload.status").value("PENDING"));
    }

    @Test
    void getLeavesReturnsList() throws Exception {
        when(leaveService.getLeaves(EMPLOYEE_ID, LeaveStatus.PENDING)).thenReturn(List.of(sampleLeave()));

        mockMvc.perform(get("/api/v1/leaves")
                        .param("employeeId", EMPLOYEE_ID.toString())
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload[0].type").value("ANNUAL"));
    }

    @Test
    void approveLeaveReturnsUpdated() throws Exception {
        LeaveResponse approved = sampleLeave();
        approved.setStatus(LeaveStatus.APPROVED);
        when(leaveService.approveLeave(leaveId)).thenReturn(approved);

        mockMvc.perform(patch("/api/v1/leaves/{id}/approve", leaveId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.status").value("APPROVED"));
    }

    @Test
    void rejectLeaveReturnsUpdated() throws Exception {
        LeaveResponse rejected = sampleLeave();
        rejected.setStatus(LeaveStatus.REJECTED);
        when(leaveService.rejectLeave(leaveId)).thenReturn(rejected);

        mockMvc.perform(patch("/api/v1/leaves/{id}/reject", leaveId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.status").value("REJECTED"));
    }

    private LeaveRequest sampleRequest() {
        return LeaveRequest.builder()
                .employeeId(EMPLOYEE_ID)
                .type(LeaveType.ANNUAL)
                .startDate(LocalDate.of(2026, 10, 10))
                .endDate(LocalDate.of(2026, 10, 12))
                .reason("Family trip")
                .build();
    }

    private LeaveResponse sampleLeave() {
        return LeaveResponse.builder()
                .id(leaveId)
                .employeeId(EMPLOYEE_ID)
                .type(LeaveType.ANNUAL)
                .startDate(LocalDate.of(2026, 10, 10))
                .endDate(LocalDate.of(2026, 10, 12))
                .reason("Family trip")
                .status(LeaveStatus.PENDING)
                .build();
    }
}

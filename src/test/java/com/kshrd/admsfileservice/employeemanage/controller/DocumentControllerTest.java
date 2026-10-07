package com.kshrd.admsfileservice.employeemanage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kshrd.admsfileservice.employeemanage.exception.GlobalExceptionHandler;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.DocumentRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.DocumentResponse;
import com.kshrd.admsfileservice.employeemanage.model.enums.DocumentType;
import com.kshrd.admsfileservice.employeemanage.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static com.kshrd.admsfileservice.employeemanage.controller.EmployeeTestData.EMPLOYEE_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
@Import(GlobalExceptionHandler.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private DocumentService documentService;

    @Test
    void createDocumentReturnsCreated() throws Exception {
        when(documentService.createDocument(any(DocumentRequest.class))).thenReturn(DocumentResponse.builder()
                .id(UUID.randomUUID())
                .employeeId(EMPLOYEE_ID)
                .title("Contract")
                .fileUrl("https://files.example.com/contract.pdf")
                .documentType(DocumentType.CONTRACT)
                .uploadedAt(Instant.parse("2026-10-07T06:00:00Z"))
                .build());

        mockMvc.perform(post("/api/v1/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(DocumentRequest.builder()
                                .employeeId(EMPLOYEE_ID)
                                .title("Contract")
                                .fileUrl("https://files.example.com/contract.pdf")
                                .documentType(DocumentType.CONTRACT)
                                .build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.payload.documentType").value("CONTRACT"));
    }
}

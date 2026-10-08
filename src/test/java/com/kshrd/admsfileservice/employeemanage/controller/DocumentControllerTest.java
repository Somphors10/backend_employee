package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.exception.GlobalExceptionHandler;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.DocumentResponse;
import com.kshrd.admsfileservice.employeemanage.model.enums.DocumentType;
import com.kshrd.admsfileservice.employeemanage.service.DocumentService;
import com.kshrd.admsfileservice.employeemanage.service.StoredDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

import static com.kshrd.admsfileservice.employeemanage.controller.EmployeeTestData.EMPLOYEE_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentService documentService;

    private final UUID documentId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Test
    void createDocumentReturnsCreated() throws Exception {
        when(documentService.createDocument(eq(EMPLOYEE_ID), eq("Contract"), eq(DocumentType.CONTRACT), any(MultipartFile.class)))
                .thenReturn(sampleDocument());

        mockMvc.perform(multipart("/api/v1/documents")
                        .file(sampleFile())
                        .param("employeeId", EMPLOYEE_ID.toString())
                        .param("title", "Contract")
                        .param("documentType", "CONTRACT"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.payload.documentType").value("CONTRACT"))
                .andExpect(jsonPath("$.payload.hasFile").value(true))
                .andExpect(jsonPath("$.payload.fileUrl").value("/api/v1/documents/" + documentId + "/file"));
    }

    @Test
    void updateDocumentReturnsOk() throws Exception {
        when(documentService.updateDocument(eq(documentId), eq("Updated contract"), isNull(), any(MultipartFile.class)))
                .thenReturn(sampleDocument());

        mockMvc.perform(multipart("/api/v1/documents/{id}", documentId)
                        .file(sampleFile())
                        .param("title", "Updated contract")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Document updated successfully"));
    }

    @Test
    void downloadFileReturnsContent() throws Exception {
        when(documentService.getFile(documentId)).thenReturn(new StoredDocument(
                new ByteArrayResource("hello".getBytes()),
                "contract.pdf",
                "application/pdf",
                5
        ));

        mockMvc.perform(get("/api/v1/documents/{id}/file", documentId))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("contract.pdf")));
    }

    private DocumentResponse sampleDocument() {
        return DocumentResponse.builder()
                .id(documentId)
                .employeeId(EMPLOYEE_ID)
                .title("Contract")
                .fileUrl("/api/v1/documents/" + documentId + "/file")
                .originalFileName("contract.pdf")
                .contentType("application/pdf")
                .fileSize(5L)
                .hasFile(true)
                .documentType(DocumentType.CONTRACT)
                .uploadedAt(Instant.parse("2026-10-07T06:00:00Z"))
                .build();
    }

    private MockMultipartFile sampleFile() {
        return new MockMultipartFile(
                "file",
                "contract.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "hello".getBytes()
        );
    }
}

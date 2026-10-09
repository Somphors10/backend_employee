package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.model.dto.response.ApiResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.DocumentResponse;
import com.kshrd.admsfileservice.employeemanage.model.enums.DocumentType;
import com.kshrd.admsfileservice.employeemanage.service.DocumentService;
import com.kshrd.admsfileservice.employeemanage.service.StoredDocument;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Documents", description = "Upload, download, update, and delete employee files")
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('documents:view')")
    @Operation(summary = "Get documents")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getDocuments(
            @RequestParam(required = false) UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.of(
                "Documents retrieved successfully",
                documentService.getDocuments(employeeId),
                HttpStatus.OK));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('documents:view')")
    @Operation(summary = "Get document by ID")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocumentById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.of(
                "Document retrieved successfully",
                documentService.getDocumentById(id),
                HttpStatus.OK));
    }

    @GetMapping("/{id}/file")
    @PreAuthorize("hasAuthority('documents:view')")
    @Operation(summary = "Download or open the uploaded file")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID id) {
        StoredDocument file = documentService.getFile(id);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (file.contentType() != null && !file.contentType().isBlank()) {
            mediaType = MediaType.parseMediaType(file.contentType());
        }
        ContentDisposition disposition = ContentDisposition.inline()
                .filename(file.originalFileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentLength(file.fileSize())
                .body(file.resource());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('documents:write')")
    @Operation(summary = "Upload a document file")
    public ResponseEntity<ApiResponse<DocumentResponse>> createDocument(
            @RequestParam UUID employeeId,
            @RequestParam String title,
            @RequestParam DocumentType documentType,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        "Document uploaded successfully",
                        documentService.createDocument(employeeId, title, documentType, file),
                        HttpStatus.CREATED));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('documents:write')")
    @Operation(summary = "Update document details or replace the file")
    public ResponseEntity<ApiResponse<DocumentResponse>> updateDocument(
            @PathVariable UUID id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) DocumentType documentType,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.of(
                "Document updated successfully",
                documentService.updateDocument(id, title, documentType, file),
                HttpStatus.OK));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('documents:write')")
    @Operation(summary = "Delete a document and its file")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable UUID id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok(ApiResponse.of("Document deleted successfully", null, HttpStatus.OK));
    }
}

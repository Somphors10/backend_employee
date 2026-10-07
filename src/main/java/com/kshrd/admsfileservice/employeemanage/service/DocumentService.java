package com.kshrd.admsfileservice.employeemanage.service;

import com.kshrd.admsfileservice.employeemanage.model.dto.request.DocumentRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.DocumentResponse;

import java.util.List;
import java.util.UUID;

public interface DocumentService {
    List<DocumentResponse> getDocuments(UUID employeeId);

    DocumentResponse getDocumentById(UUID id);

    DocumentResponse createDocument(DocumentRequest request);

    void deleteDocument(UUID id);
}

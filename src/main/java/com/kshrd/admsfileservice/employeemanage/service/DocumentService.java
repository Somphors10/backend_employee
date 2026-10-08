package com.kshrd.admsfileservice.employeemanage.service;

import com.kshrd.admsfileservice.employeemanage.model.dto.response.DocumentResponse;
import com.kshrd.admsfileservice.employeemanage.model.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface DocumentService {
    List<DocumentResponse> getDocuments(UUID employeeId);

    DocumentResponse getDocumentById(UUID id);

    StoredDocument getFile(UUID id);

    DocumentResponse createDocument(UUID employeeId, String title, DocumentType documentType, MultipartFile file);

    DocumentResponse updateDocument(UUID id, String title, DocumentType documentType, MultipartFile file);

    void deleteDocument(UUID id);
}

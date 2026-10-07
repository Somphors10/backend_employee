package com.kshrd.admsfileservice.employeemanage.service.impl;

import com.kshrd.admsfileservice.employeemanage.exception.ResourceNotFoundException;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.DocumentRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.DocumentResponse;
import com.kshrd.admsfileservice.employeemanage.model.entity.EmployeeDocument;
import com.kshrd.admsfileservice.employeemanage.repository.EmployeeDocumentRepository;
import com.kshrd.admsfileservice.employeemanage.service.DocumentService;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DocumentServiceImpl implements DocumentService {
    private final EmployeeDocumentRepository documentRepository;
    private final EmployeeService employeeService;

    public DocumentServiceImpl(EmployeeDocumentRepository documentRepository, EmployeeService employeeService) {
        this.documentRepository = documentRepository;
        this.employeeService = employeeService;
    }

    @Override
    public List<DocumentResponse> getDocuments(UUID employeeId) {
        List<EmployeeDocument> documents = employeeId == null
                ? documentRepository.findAll()
                : documentRepository.findByEmployeeId(employeeId);
        return documents.stream().map(DocumentResponse::from).toList();
    }

    @Override
    public DocumentResponse getDocumentById(UUID id) {
        return DocumentResponse.from(findDocument(id));
    }

    @Override
    public DocumentResponse createDocument(DocumentRequest request) {
        employeeService.getEmployeeById(request.getEmployeeId());
        EmployeeDocument document = EmployeeDocument.builder()
                .id(UUID.randomUUID())
                .employeeId(request.getEmployeeId())
                .title(request.getTitle().trim())
                .fileUrl(request.getFileUrl().trim())
                .documentType(request.getDocumentType())
                .uploadedAt(Instant.now())
                .build();
        return DocumentResponse.from(documentRepository.save(document));
    }

    @Override
    public void deleteDocument(UUID id) {
        documentRepository.delete(findDocument(id));
    }

    private EmployeeDocument findDocument(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));
    }
}

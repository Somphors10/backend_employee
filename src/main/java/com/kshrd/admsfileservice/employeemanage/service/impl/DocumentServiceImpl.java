package com.kshrd.admsfileservice.employeemanage.service.impl;

import com.kshrd.admsfileservice.employeemanage.exception.InvalidOperationException;
import com.kshrd.admsfileservice.employeemanage.exception.ResourceNotFoundException;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.DocumentResponse;
import com.kshrd.admsfileservice.employeemanage.model.entity.EmployeeDocument;
import com.kshrd.admsfileservice.employeemanage.model.enums.DocumentType;
import com.kshrd.admsfileservice.employeemanage.repository.EmployeeDocumentRepository;
import com.kshrd.admsfileservice.employeemanage.security.AccessService;
import com.kshrd.admsfileservice.employeemanage.service.DocumentService;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import com.kshrd.admsfileservice.employeemanage.service.FileStorageService;
import com.kshrd.admsfileservice.employeemanage.service.StoredDocument;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class DocumentServiceImpl implements DocumentService {
    private final EmployeeDocumentRepository documentRepository;
    private final EmployeeService employeeService;
    private final FileStorageService fileStorageService;
    private final AccessService accessService;

    public DocumentServiceImpl(
            EmployeeDocumentRepository documentRepository,
            EmployeeService employeeService,
            FileStorageService fileStorageService,
            AccessService accessService) {
        this.documentRepository = documentRepository;
        this.employeeService = employeeService;
        this.fileStorageService = fileStorageService;
        this.accessService = accessService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocuments(UUID employeeId) {
        UUID scoped = accessService.resolveEmployeeId(employeeId);
        Set<UUID> visible = accessService.visibleEmployeeIds();
        List<EmployeeDocument> documents = scoped == null
                ? documentRepository.findAll()
                : documentRepository.findByEmployeeId(scoped);
        return documents.stream()
                .filter(document -> visible == null || visible.contains(document.getEmployeeId()))
                .map(DocumentResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(UUID id) {
        EmployeeDocument document = findDocument(id);
        accessService.assertCanViewEmployee(document.getEmployeeId());
        return DocumentResponse.from(document);
    }

    @Override
    @Transactional(readOnly = true)
    public StoredDocument getFile(UUID id) {
        EmployeeDocument document = findDocument(id);
        accessService.assertCanViewEmployee(document.getEmployeeId());
        if (document.getStoredFileName() == null || document.getStoredFileName().isBlank()) {
            throw new ResourceNotFoundException("Uploaded file not found for this document");
        }
        Resource resource = fileStorageService.load(document.getStoredFileName());
        String name = document.getOriginalFileName() == null ? document.getStoredFileName() : document.getOriginalFileName();
        String contentType = document.getContentType() == null ? "application/octet-stream" : document.getContentType();
        long size = document.getFileSize() == null ? 0 : document.getFileSize();
        try {
            long actual = resource.contentLength();
            if (actual > 0) {
                size = actual;
            }
        } catch (java.io.IOException ignored) {
            // Keep the stored size when the resource length is unavailable.
        }
        return new StoredDocument(resource, name, contentType, size);
    }

    @Override
    public DocumentResponse createDocument(UUID employeeId, String title, DocumentType documentType, MultipartFile file) {
        employeeService.getEmployeeById(employeeId);
        if (title == null || title.isBlank()) {
            throw new InvalidOperationException("Title is required");
        }
        if (documentType == null) {
            throw new InvalidOperationException("Document type is required");
        }
        UUID id = UUID.randomUUID();
        String storedName = fileStorageService.store(id, file);
        EmployeeDocument document = EmployeeDocument.builder()
                .id(id)
                .employeeId(employeeId)
                .title(title.trim())
                .fileUrl("/api/v1/documents/" + id + "/file")
                .originalFileName(safeOriginalName(file.getOriginalFilename()))
                .storedFileName(storedName)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .documentType(documentType)
                .uploadedAt(Instant.now())
                .build();
        return DocumentResponse.from(documentRepository.save(document));
    }

    @Override
    public DocumentResponse updateDocument(UUID id, String title, DocumentType documentType, MultipartFile file) {
        EmployeeDocument document = findDocument(id);
        if (title != null && !title.isBlank()) {
            document.setTitle(title.trim());
        }
        if (documentType != null) {
            document.setDocumentType(documentType);
        }
        if (file != null && !file.isEmpty()) {
            fileStorageService.delete(document.getStoredFileName());
            String storedName = fileStorageService.store(document.getId(), file);
            document.setStoredFileName(storedName);
            document.setOriginalFileName(safeOriginalName(file.getOriginalFilename()));
            document.setContentType(file.getContentType());
            document.setFileSize(file.getSize());
            document.setFileUrl("/api/v1/documents/" + document.getId() + "/file");
            document.setUploadedAt(Instant.now());
        }
        return DocumentResponse.from(documentRepository.save(document));
    }

    @Override
    public void deleteDocument(UUID id) {
        EmployeeDocument document = findDocument(id);
        fileStorageService.delete(document.getStoredFileName());
        documentRepository.delete(document);
    }

    private EmployeeDocument findDocument(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));
    }

    private String safeOriginalName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "document";
        }
        return extractFileName(originalFilename);
    }

    private String extractFileName(String originalFilename) {
        String name = originalFilename.replace("\\", "/");
        int slash = name.lastIndexOf('/');
        return slash >= 0 ? name.substring(slash + 1) : name;
    }
}

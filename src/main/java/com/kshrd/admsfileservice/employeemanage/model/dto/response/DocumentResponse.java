package com.kshrd.admsfileservice.employeemanage.model.dto.response;

import com.kshrd.admsfileservice.employeemanage.model.entity.EmployeeDocument;
import com.kshrd.admsfileservice.employeemanage.model.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private UUID id;
    private UUID employeeId;
    private String title;
    private String fileUrl;
    private DocumentType documentType;
    private Instant uploadedAt;

    public static DocumentResponse from(EmployeeDocument document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .employeeId(document.getEmployeeId())
                .title(document.getTitle())
                .fileUrl(document.getFileUrl())
                .documentType(document.getDocumentType())
                .uploadedAt(document.getUploadedAt())
                .build();
    }
}

package com.kshrd.admsfileservice.employeemanage.model.dto.request;

import com.kshrd.admsfileservice.employeemanage.model.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequest {
    @NotNull(message = "Employee id is required")
    private UUID employeeId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "File URL is required")
    private String fileUrl;

    @NotNull(message = "Document type is required")
    private DocumentType documentType;
}

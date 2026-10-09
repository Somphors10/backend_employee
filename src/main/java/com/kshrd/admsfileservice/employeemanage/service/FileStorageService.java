package com.kshrd.admsfileservice.employeemanage.service;

import com.kshrd.admsfileservice.employeemanage.exception.InvalidOperationException;
import com.kshrd.admsfileservice.employeemanage.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "png", "jpg", "jpeg", "doc", "docx", "webp"
    );

    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload.dir}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not create upload directory", ex);
        }
    }

    public String store(UUID documentId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidOperationException("File is required");
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidOperationException(
                    "File type is not allowed. Use PDF, PNG, JPG, DOC, DOCX, or WEBP");
        }
        String storedName = documentId + "." + extension;
        Path target = uploadDir.resolve(storedName).normalize();
        if (!target.startsWith(uploadDir)) {
            throw new InvalidOperationException("Invalid file path");
        }
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new InvalidOperationException("Could not store the file");
        }
        return storedName;
    }

    public String storeContent(UUID documentId, String originalFilename, byte[] content) {
        if (content == null || content.length == 0) {
            throw new InvalidOperationException("File is required");
        }
        String extension = extensionOf(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidOperationException(
                    "File type is not allowed. Use PDF, PNG, JPG, DOC, DOCX, or WEBP");
        }
        String storedName = documentId + "." + extension;
        Path target = uploadDir.resolve(storedName).normalize();
        if (!target.startsWith(uploadDir)) {
            throw new InvalidOperationException("Invalid file path");
        }
        try {
            Files.write(target, content);
        } catch (IOException ex) {
            throw new InvalidOperationException("Could not store the file");
        }
        return storedName;
    }

    public Resource load(String storedFileName) {
        Path file = uploadDir.resolve(storedFileName).normalize();
        if (!file.startsWith(uploadDir) || !Files.exists(file)) {
            throw new ResourceNotFoundException("Document file not found");
        }
        try {
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Document file not found");
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Document file not found");
        }
    }

    public void delete(String storedFileName) {
        if (storedFileName == null || storedFileName.isBlank()) {
            return;
        }
        Path file = uploadDir.resolve(storedFileName).normalize();
        if (!file.startsWith(uploadDir)) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // Keep the database delete even if the file is already gone.
        }
    }

    private String extensionOf(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new InvalidOperationException("File must have an extension");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
        if (extension.isBlank()) {
            throw new InvalidOperationException("File must have an extension");
        }
        return extension;
    }
}

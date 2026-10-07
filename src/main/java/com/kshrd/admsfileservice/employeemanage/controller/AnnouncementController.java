package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.model.dto.request.AnnouncementRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.AnnouncementResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.ApiResponse;
import com.kshrd.admsfileservice.employeemanage.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/announcements")
@Tag(name = "Announcements", description = "Company announcements")
public class AnnouncementController {
    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    @Operation(summary = "Get announcements")
    public ResponseEntity<ApiResponse<List<AnnouncementResponse>>> getAnnouncements() {
        return ResponseEntity.ok(ApiResponse.of(
                "Announcements retrieved successfully",
                announcementService.getAnnouncements(),
                HttpStatus.OK));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get announcement by ID")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> getAnnouncementById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.of(
                "Announcement retrieved successfully",
                announcementService.getAnnouncementById(id),
                HttpStatus.OK));
    }

    @PostMapping
    @Operation(summary = "Create an announcement")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> createAnnouncement(
            @Valid @RequestBody AnnouncementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        "Announcement created successfully",
                        announcementService.createAnnouncement(request),
                        HttpStatus.CREATED));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an announcement")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> updateAnnouncement(
            @PathVariable UUID id,
            @Valid @RequestBody AnnouncementRequest request) {
        return ResponseEntity.ok(ApiResponse.of(
                "Announcement updated successfully",
                announcementService.updateAnnouncement(id, request),
                HttpStatus.OK));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an announcement")
    public ResponseEntity<ApiResponse<Void>> deleteAnnouncement(@PathVariable UUID id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.ok(ApiResponse.of("Announcement deleted successfully", null, HttpStatus.OK));
    }
}

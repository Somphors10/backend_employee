package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.model.dto.response.ApiResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.NotificationResponse;
import com.kshrd.admsfileservice.employeemanage.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "In-app notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('notifications:view')")
    @Operation(summary = "Get my notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> mine() {
        return ResponseEntity.ok(ApiResponse.of(
                "Notifications retrieved successfully",
                notificationService.getMine(),
                HttpStatus.OK));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAuthority('notifications:view')")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markRead(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.of(
                "Notification marked as read",
                notificationService.markRead(id),
                HttpStatus.OK));
    }
}

package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.model.dto.request.SettingRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.ApiResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.SettingResponse;
import com.kshrd.admsfileservice.employeemanage.service.SettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/settings")
@Tag(name = "Settings", description = "Application settings")
public class SettingController {
    private final SettingService settingService;

    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('settings:view')")
    @Operation(summary = "Get all settings")
    public ResponseEntity<ApiResponse<List<SettingResponse>>> getSettings() {
        return ResponseEntity.ok(ApiResponse.of(
                "Settings retrieved successfully",
                settingService.getSettings(),
                HttpStatus.OK));
    }

    @GetMapping("/{key}")
    @PreAuthorize("hasAuthority('settings:view')")
    @Operation(summary = "Get a setting by key")
    public ResponseEntity<ApiResponse<SettingResponse>> getSetting(@PathVariable String key) {
        return ResponseEntity.ok(ApiResponse.of(
                "Setting retrieved successfully",
                settingService.getSetting(key),
                HttpStatus.OK));
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasAuthority('settings:write')")
    @Operation(summary = "Create or update a setting")
    public ResponseEntity<ApiResponse<SettingResponse>> upsertSetting(
            @PathVariable String key,
            @Valid @RequestBody SettingRequest request) {
        return ResponseEntity.ok(ApiResponse.of(
                "Setting saved successfully",
                settingService.upsertSetting(key, request),
                HttpStatus.OK));
    }
}

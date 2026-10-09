package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.model.dto.response.ApiResponse;
import com.kshrd.admsfileservice.employeemanage.security.AccessService;
import com.kshrd.admsfileservice.employeemanage.security.Permission;
import com.kshrd.admsfileservice.employeemanage.security.RolePermissions;
import com.kshrd.admsfileservice.employeemanage.security.RolePermissions.RoleAccess;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rbac")
@Tag(name = "RBAC", description = "Roles, permissions, and access matrix")
public class RbacController {
    private final AccessService accessService;

    public RbacController(AccessService accessService) {
        this.accessService = accessService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get the current user's role and permissions")
    public ResponseEntity<ApiResponse<RoleAccess>> me() {
        var user = accessService.currentUser();
        RoleAccess payload = user == null
                ? new RoleAccess(null, List.of())
                : new RoleAccess(user.getRole().name(), RolePermissions.forRole(user.getRole()));
        return ResponseEntity.ok(ApiResponse.of("Current access retrieved successfully", payload, HttpStatus.OK));
    }

    @GetMapping("/matrix")
    @PreAuthorize("hasAuthority('roles:view')")
    @Operation(summary = "Get the full role-permission matrix")
    public ResponseEntity<ApiResponse<RbacMatrix>> matrix() {
        RbacMatrix payload = new RbacMatrix(Permission.all(), RolePermissions.matrix());
        return ResponseEntity.ok(ApiResponse.of("RBAC matrix retrieved successfully", payload, HttpStatus.OK));
    }

    public record RbacMatrix(List<String> permissions, List<RoleAccess> roles) {
    }
}

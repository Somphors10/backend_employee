package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.exception.InvalidOperationException;
import com.kshrd.admsfileservice.employeemanage.exception.ResourceNotFoundException;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.ApiResponse;
import com.kshrd.admsfileservice.employeemanage.model.entity.AppUser;
import com.kshrd.admsfileservice.employeemanage.model.enums.Role;
import com.kshrd.admsfileservice.employeemanage.repository.AppUserRepository;
import com.kshrd.admsfileservice.employeemanage.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Login accounts")
public class UserController {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeService employeeService;

    public UserController(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            EmployeeService employeeService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.employeeService = employeeService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('users:write')")
    @Operation(summary = "List login accounts")
    public ResponseEntity<ApiResponse<List<UserView>>> getUsers() {
        List<UserView> users = appUserRepository.findAll().stream()
                .sorted(Comparator.comparing(AppUser::getUsername))
                .map(UserView::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.of("Users retrieved successfully", users, HttpStatus.OK));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('users:write')")
    @Operation(summary = "Create a login account")
    public ResponseEntity<ApiResponse<UserView>> create(@RequestBody UserBody body) {
        if (body.username() == null || body.password() == null || body.role() == null) {
            throw new InvalidOperationException("Username, password, and role are required");
        }
        appUserRepository.findByUsernameIgnoreCase(body.username()).ifPresent(existing -> {
            throw new InvalidOperationException("Username is already taken");
        });
        if (body.employeeId() != null) {
            employeeService.getEmployeeById(body.employeeId());
        }
        AppUser saved = appUserRepository.save(AppUser.builder()
                .id(UUID.randomUUID())
                .username(body.username().trim())
                .password(passwordEncoder.encode(body.password()))
                .role(body.role())
                .employeeId(body.employeeId())
                .enabled(body.enabled() == null || body.enabled())
                .build());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("User created successfully", UserView.from(saved), HttpStatus.CREATED));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('users:write')")
    @Operation(summary = "Update a login account")
    public ResponseEntity<ApiResponse<UserView>> update(@PathVariable UUID id, @RequestBody UserBody body) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        if (body.role() != null) {
            user.setRole(body.role());
        }
        if (body.enabled() != null) {
            user.setEnabled(body.enabled());
        }
        if (body.employeeId() != null) {
            employeeService.getEmployeeById(body.employeeId());
            user.setEmployeeId(body.employeeId());
        }
        if (body.password() != null && !body.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(body.password()));
        }
        return ResponseEntity.ok(ApiResponse.of("User updated successfully", UserView.from(appUserRepository.save(user)), HttpStatus.OK));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('users:write')")
    @Operation(summary = "Delete a login account")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        appUserRepository.delete(user);
        return ResponseEntity.ok(ApiResponse.of("User deleted successfully", null, HttpStatus.OK));
    }

    public record UserBody(String username, String password, Role role, UUID employeeId, Boolean enabled) {
    }

    public record UserView(UUID id, String username, Role role, UUID employeeId, boolean enabled) {
        static UserView from(AppUser user) {
            return new UserView(user.getId(), user.getUsername(), user.getRole(), user.getEmployeeId(), user.isEnabled());
        }
    }
}

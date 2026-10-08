package com.kshrd.admsfileservice.employeemanage.service.impl;

import com.kshrd.admsfileservice.employeemanage.exception.InvalidCredentialsException;
import com.kshrd.admsfileservice.employeemanage.exception.InvalidOperationException;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.ChangePasswordRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.LoginRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.AuthResponse;
import com.kshrd.admsfileservice.employeemanage.model.entity.AppUser;
import com.kshrd.admsfileservice.employeemanage.repository.AppUserRepository;
import com.kshrd.admsfileservice.employeemanage.security.AppUserDetails;
import com.kshrd.admsfileservice.employeemanage.security.JwtService;
import com.kshrd.admsfileservice.employeemanage.security.RolePermissions;
import com.kshrd.admsfileservice.employeemanage.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByUsernameIgnoreCase(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));
        if (!user.isEnabled() || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        return toResponse(user, jwtService.generateToken(user));
    }

    @Override
    public AuthResponse currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails details)) {
            throw new InvalidCredentialsException("Please login to access this resource");
        }
        AppUser user = details.getUser();
        return toResponse(user, jwtService.generateToken(user));
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails details)) {
            throw new InvalidCredentialsException("Please login to access this resource");
        }
        AppUser user = appUserRepository.findById(details.getUser().getId())
                .orElseThrow(() -> new InvalidCredentialsException("Please login to access this resource"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidOperationException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        appUserRepository.save(user);
    }

    private AuthResponse toResponse(AppUser user, String token) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .role(user.getRole())
                .employeeId(user.getEmployeeId())
                .permissions(RolePermissions.forRole(user.getRole()))
                .build();
    }
}

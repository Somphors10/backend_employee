package com.kshrd.admsfileservice.employeemanage.service;

import com.kshrd.admsfileservice.employeemanage.model.dto.request.LoginRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);

    AuthResponse currentUser();
}

package com.kshrd.admsfileservice.employeemanage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kshrd.admsfileservice.employeemanage.exception.GlobalExceptionHandler;
import com.kshrd.admsfileservice.employeemanage.exception.InvalidCredentialsException;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.LoginRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.AuthResponse;
import com.kshrd.admsfileservice.employeemanage.model.enums.Role;
import com.kshrd.admsfileservice.employeemanage.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private AuthService authService;

    @Test
    void loginReturnsToken() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(AuthResponse.builder()
                .token("jwt-token")
                .tokenType("Bearer")
                .username("admin")
                .role(Role.ADMIN)
                .build());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.token").value("jwt-token"))
                .andExpect(jsonPath("$.payload.role").value("ADMIN"));
    }

    @Test
    void loginRejectsBadCredentials() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin", "wrong"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }
}

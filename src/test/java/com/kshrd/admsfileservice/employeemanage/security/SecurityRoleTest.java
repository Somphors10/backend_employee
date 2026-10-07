package com.kshrd.admsfileservice.employeemanage.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.LoginRequest;
import com.kshrd.admsfileservice.employeemanage.model.entity.AppUser;
import com.kshrd.admsfileservice.employeemanage.model.enums.Role;
import com.kshrd.admsfileservice.employeemanage.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityRoleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        saveUser("sec-admin", "admin123", Role.ADMIN);
        saveUser("sec-employee", "employee123", Role.EMPLOYEE);
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void employeeCannotReadPayroll() throws Exception {
        String token = login("sec-employee", "employee123");

        mockMvc.perform(get("/api/v1/payrolls").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanReadPayroll() throws Exception {
        String token = login("sec-admin", "admin123");

        mockMvc.perform(get("/api/v1/payrolls").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void employeeCannotCreateEmployee() throws Exception {
        String token = login("sec-employee", "employee123");

        mockMvc.perform(post("/api/v1/employees")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Test",
                                  "lastName": "User",
                                  "email": "test.user@company.com",
                                  "phoneNumber": "012000000",
                                  "position": "Intern",
                                  "department": "IT",
                                  "hireDate": "2026-01-01"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    private void saveUser(String username, String password, Role role) {
        if (appUserRepository.findByUsernameIgnoreCase(username).isPresent()) {
            return;
        }
        appUserRepository.save(AppUser.builder()
                .id(UUID.randomUUID())
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(role)
                .enabled(true)
                .build());
    }

    private String login(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(username, password))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body).path("payload").path("token").asText();
    }
}

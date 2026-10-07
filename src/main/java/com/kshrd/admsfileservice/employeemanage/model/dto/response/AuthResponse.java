package com.kshrd.admsfileservice.employeemanage.model.dto.response;

import com.kshrd.admsfileservice.employeemanage.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType;
    private String username;
    private Role role;
    private UUID employeeId;
    private List<String> permissions;
}

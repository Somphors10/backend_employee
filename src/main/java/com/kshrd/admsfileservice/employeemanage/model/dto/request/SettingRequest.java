package com.kshrd.admsfileservice.employeemanage.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingRequest {
    @NotBlank(message = "Value is required")
    private String value;
}

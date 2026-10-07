package com.kshrd.admsfileservice.employeemanage.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationItemResponse {
    private String key;
    private String label;
    private String path;
    private String status;
}

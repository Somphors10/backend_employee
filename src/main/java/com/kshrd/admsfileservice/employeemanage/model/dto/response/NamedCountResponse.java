package com.kshrd.admsfileservice.employeemanage.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NamedCountResponse {
    private String name;
    private long count;
    private BigDecimal amount;
    private double percent;
}

package com.kshrd.admsfileservice.employeemanage.model.dto.response;

import com.kshrd.admsfileservice.employeemanage.model.entity.EmployeeHistory;
import com.kshrd.admsfileservice.employeemanage.model.enums.EmployeeEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeHistoryResponse {
    private UUID id;
    private UUID employeeId;
    private EmployeeEventType eventType;
    private String description;
    private Instant occurredAt;

    public static EmployeeHistoryResponse from(EmployeeHistory history) {
        return EmployeeHistoryResponse.builder()
                .id(history.getId())
                .employeeId(history.getEmployeeId())
                .eventType(history.getEventType())
                .description(history.getDescription())
                .occurredAt(history.getOccurredAt())
                .build();
    }
}

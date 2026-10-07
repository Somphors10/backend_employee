package com.kshrd.admsfileservice.employeemanage.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private Integer status;
    private String message;
    private T payload;
    private Instant timestamp;

    public static <T> ApiResponse<T> of(String message, T payload, HttpStatus status) {
        return ApiResponse.<T>builder()
                .status(status.value())
                .message(message)
                .payload(payload)
                .timestamp(Instant.now())
                .build();
    }
}

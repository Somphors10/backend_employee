package com.kshrd.admsfileservice.employeemanage.model.dto.response;

import com.kshrd.admsfileservice.employeemanage.model.entity.AppNotification;
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
public class NotificationResponse {
    private UUID id;
    private String title;
    private String message;
    private boolean read;
    private Instant createdAt;

    public static NotificationResponse from(AppNotification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .read(notification.isReadFlag())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

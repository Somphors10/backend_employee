package com.kshrd.admsfileservice.employeemanage.service;

import com.kshrd.admsfileservice.employeemanage.model.dto.response.NotificationResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    List<NotificationResponse> getMine();

    NotificationResponse markRead(UUID id);

    void notifyEmployee(UUID employeeId, String title, String message);
}

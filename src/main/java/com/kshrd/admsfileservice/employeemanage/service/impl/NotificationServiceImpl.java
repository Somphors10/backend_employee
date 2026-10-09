package com.kshrd.admsfileservice.employeemanage.service.impl;

import com.kshrd.admsfileservice.employeemanage.exception.InvalidCredentialsException;
import com.kshrd.admsfileservice.employeemanage.exception.ResourceNotFoundException;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.NotificationResponse;
import com.kshrd.admsfileservice.employeemanage.model.entity.AppNotification;
import com.kshrd.admsfileservice.employeemanage.model.entity.AppUser;
import com.kshrd.admsfileservice.employeemanage.repository.AppNotificationRepository;
import com.kshrd.admsfileservice.employeemanage.repository.AppUserRepository;
import com.kshrd.admsfileservice.employeemanage.security.AccessService;
import com.kshrd.admsfileservice.employeemanage.service.NotificationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {
    private final AppNotificationRepository notificationRepository;
    private final AppUserRepository appUserRepository;
    private final AccessService accessService;

    public NotificationServiceImpl(
            AppNotificationRepository notificationRepository,
            AppUserRepository appUserRepository,
            AccessService accessService) {
        this.notificationRepository = notificationRepository;
        this.appUserRepository = appUserRepository;
        this.accessService = accessService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMine() {
        AppUser user = requireUser();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Override
    public NotificationResponse markRead(UUID id) {
        AppUser user = requireUser();
        AppNotification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        if (!notification.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("You cannot read this notification");
        }
        notification.setReadFlag(true);
        return NotificationResponse.from(notificationRepository.save(notification));
    }

    @Override
    public void notifyEmployee(UUID employeeId, String title, String message) {
        if (employeeId == null) {
            return;
        }
        appUserRepository.findByEmployeeId(employeeId).ifPresent(user -> notificationRepository.save(
                AppNotification.builder()
                        .id(UUID.randomUUID())
                        .userId(user.getId())
                        .title(title)
                        .message(message)
                        .readFlag(false)
                        .createdAt(Instant.now())
                        .build()));
    }

    private AppUser requireUser() {
        AppUser user = accessService.currentUser();
        if (user == null) {
            throw new InvalidCredentialsException("Please login to access this resource");
        }
        return user;
    }
}

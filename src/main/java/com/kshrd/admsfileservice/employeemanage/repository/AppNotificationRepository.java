package com.kshrd.admsfileservice.employeemanage.repository;

import com.kshrd.admsfileservice.employeemanage.model.entity.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppNotificationRepository extends JpaRepository<AppNotification, UUID> {
    List<AppNotification> findByUserIdOrderByCreatedAtDesc(UUID userId);
}

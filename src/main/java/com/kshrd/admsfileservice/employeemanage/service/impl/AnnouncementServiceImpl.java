package com.kshrd.admsfileservice.employeemanage.service.impl;

import com.kshrd.admsfileservice.employeemanage.exception.ResourceNotFoundException;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.AnnouncementRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.AnnouncementResponse;
import com.kshrd.admsfileservice.employeemanage.model.entity.Announcement;
import com.kshrd.admsfileservice.employeemanage.repository.AnnouncementRepository;
import com.kshrd.admsfileservice.employeemanage.service.AnnouncementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AnnouncementServiceImpl implements AnnouncementService {
    private final AnnouncementRepository announcementRepository;

    public AnnouncementServiceImpl(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    @Override
    public List<AnnouncementResponse> getAnnouncements() {
        return announcementRepository.findAll().stream()
                .sorted(Comparator.comparing(Announcement::getCreatedAt).reversed())
                .map(AnnouncementResponse::from)
                .toList();
    }

    @Override
    public AnnouncementResponse getAnnouncementById(UUID id) {
        return AnnouncementResponse.from(findAnnouncement(id));
    }

    @Override
    public AnnouncementResponse createAnnouncement(AnnouncementRequest request) {
        Announcement announcement = Announcement.builder()
                .id(UUID.randomUUID())
                .title(request.getTitle().trim())
                .content(request.getContent().trim())
                .published(request.isPublished())
                .createdAt(Instant.now())
                .build();
        return AnnouncementResponse.from(announcementRepository.save(announcement));
    }

    @Override
    public AnnouncementResponse updateAnnouncement(UUID id, AnnouncementRequest request) {
        Announcement announcement = findAnnouncement(id);
        announcement.setTitle(request.getTitle().trim());
        announcement.setContent(request.getContent().trim());
        announcement.setPublished(request.isPublished());
        return AnnouncementResponse.from(announcementRepository.save(announcement));
    }

    @Override
    public void deleteAnnouncement(UUID id) {
        announcementRepository.delete(findAnnouncement(id));
    }

    private Announcement findAnnouncement(UUID id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement", id));
    }
}

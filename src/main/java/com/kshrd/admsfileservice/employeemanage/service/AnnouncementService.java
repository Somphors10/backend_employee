package com.kshrd.admsfileservice.employeemanage.service;

import com.kshrd.admsfileservice.employeemanage.model.dto.request.AnnouncementRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.AnnouncementResponse;

import java.util.List;
import java.util.UUID;

public interface AnnouncementService {
    List<AnnouncementResponse> getAnnouncements();

    AnnouncementResponse getAnnouncementById(UUID id);

    AnnouncementResponse createAnnouncement(AnnouncementRequest request);

    AnnouncementResponse updateAnnouncement(UUID id, AnnouncementRequest request);

    void deleteAnnouncement(UUID id);
}

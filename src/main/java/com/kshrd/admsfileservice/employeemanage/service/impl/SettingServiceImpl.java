package com.kshrd.admsfileservice.employeemanage.service.impl;

import com.kshrd.admsfileservice.employeemanage.exception.ResourceNotFoundException;
import com.kshrd.admsfileservice.employeemanage.model.dto.request.SettingRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.SettingResponse;
import com.kshrd.admsfileservice.employeemanage.model.entity.AppSetting;
import com.kshrd.admsfileservice.employeemanage.repository.AppSettingRepository;
import com.kshrd.admsfileservice.employeemanage.service.SettingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SettingServiceImpl implements SettingService {
    private final AppSettingRepository settingRepository;

    public SettingServiceImpl(AppSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @Override
    public List<SettingResponse> getSettings() {
        return settingRepository.findAll().stream()
                .sorted(Comparator.comparing(AppSetting::getSettingKey))
                .map(SettingResponse::from)
                .toList();
    }

    @Override
    public SettingResponse getSetting(String key) {
        return SettingResponse.from(settingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found: " + key)));
    }

    @Override
    public SettingResponse upsertSetting(String key, SettingRequest request) {
        AppSetting setting = settingRepository.findBySettingKey(key)
                .orElseGet(() -> AppSetting.builder()
                        .id(UUID.randomUUID())
                        .settingKey(key)
                        .build());
        setting.setSettingValue(request.getValue().trim());
        return SettingResponse.from(settingRepository.save(setting));
    }
}

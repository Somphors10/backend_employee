package com.kshrd.admsfileservice.employeemanage.service;

import com.kshrd.admsfileservice.employeemanage.model.dto.request.SettingRequest;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.SettingResponse;

import java.util.List;

public interface SettingService {
    List<SettingResponse> getSettings();

    SettingResponse getSetting(String key);

    SettingResponse upsertSetting(String key, SettingRequest request);
}

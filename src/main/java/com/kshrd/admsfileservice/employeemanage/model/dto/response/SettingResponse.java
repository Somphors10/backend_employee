package com.kshrd.admsfileservice.employeemanage.model.dto.response;

import com.kshrd.admsfileservice.employeemanage.model.entity.AppSetting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingResponse {
    private String key;
    private String value;

    public static SettingResponse from(AppSetting setting) {
        return SettingResponse.builder()
                .key(setting.getSettingKey())
                .value(setting.getSettingValue())
                .build();
    }
}

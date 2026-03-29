package com.osy.sharetodo.feature.device.domain;

import com.osy.sharetodo.global.exception.ApiException;

import static com.osy.sharetodo.global.exception.ErrorCode.VALIDATION_ERROR;

public enum DevicePlatform {
    IOS,
    ANDROID;

    public static DevicePlatform from(String value) {
        if (value == null || value.isBlank()) {
            throw new ApiException(VALIDATION_ERROR, "platform 값이 올바르지 않습니다.");
        }

        return DevicePlatform.valueOf(value.trim().toUpperCase());
    }
}

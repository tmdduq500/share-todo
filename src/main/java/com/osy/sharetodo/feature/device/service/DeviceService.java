package com.osy.sharetodo.feature.device.service;

import com.osy.sharetodo.feature.account.domain.Account;
import com.osy.sharetodo.feature.account.repository.AccountRepository;
import com.osy.sharetodo.feature.device.domain.DevicePlatform;
import com.osy.sharetodo.feature.device.domain.DeviceToken;
import com.osy.sharetodo.feature.device.dto.DeviceTokenRegisterRequest;
import com.osy.sharetodo.feature.device.dto.DeviceTokenUnregisterRequest;
import com.osy.sharetodo.feature.device.repository.DeviceTokenRepository;
import com.osy.sharetodo.global.exception.ApiException;
import com.osy.sharetodo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public void register(String uid, DeviceTokenRegisterRequest request) {
        Account account = accountRepository.findByUid(uid)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "계정을 찾을 수 없습니다. id=" + uid));

        DevicePlatform platform = DevicePlatform.from(request.getPlatform());

        DeviceToken deviceToken = deviceTokenRepository.findByToken(request.getToken())
                .orElseGet(() -> DeviceToken.create(account, request.getToken(), platform));

        if (deviceToken.getId() == null) {
            deviceTokenRepository.save(deviceToken);
            return;
        }

        deviceToken.reassign(account, platform);
    }

    @Transactional
    public void unregister(String uid, DeviceTokenUnregisterRequest request) {
        deviceTokenRepository.findByAccount_uidAndToken(uid, request.getToken())
                .ifPresent(deviceTokenRepository::delete);
    }
}
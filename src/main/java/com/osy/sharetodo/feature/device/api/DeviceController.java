package com.osy.sharetodo.feature.device.api;

import com.osy.sharetodo.feature.device.dto.DeviceTokenRegisterRequest;
import com.osy.sharetodo.feature.device.dto.DeviceTokenUnregisterRequest;
import com.osy.sharetodo.feature.device.service.DeviceService;
import com.osy.sharetodo.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping("/token")
    public ApiResponse<Void> registerToken(@Valid @RequestBody DeviceTokenRegisterRequest request) {
        String accountUid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        deviceService.register(accountUid, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/token")
    public ApiResponse<Void> unregisterToken(@Valid @RequestBody DeviceTokenUnregisterRequest request) {
        String accountUid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        deviceService.unregister(accountUid, request);
        return ApiResponse.ok(null);
    }
}
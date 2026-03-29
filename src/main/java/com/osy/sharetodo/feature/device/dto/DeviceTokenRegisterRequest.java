package com.osy.sharetodo.feature.device.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeviceTokenRegisterRequest {
    @NotBlank
    private String token;

    @NotBlank
    private String platform;

}
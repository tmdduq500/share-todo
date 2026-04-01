package com.osy.sharetodo.feature.notification.push.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ExpoPushMessage {
    private String to;
    private String title;
    private String body;
    private Map<String, Object> data;
    private String sound;
}

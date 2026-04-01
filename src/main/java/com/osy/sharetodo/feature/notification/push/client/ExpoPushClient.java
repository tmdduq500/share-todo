package com.osy.sharetodo.feature.notification.push.client;

import com.osy.sharetodo.feature.notification.push.dto.ExpoPushMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpoPushClient {

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    private final RestClient restClient;

    public void send(ExpoPushMessage message) {
        try {
            String response = restClient.post()
                    .uri(EXPO_PUSH_URL)
                    .headers(h -> {
                        h.setContentType(MediaType.APPLICATION_JSON);
                        h.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
                    })
                    .body(message)
                    .retrieve()
                    .body(String.class);

            log.info("[Push] Expo push sent. to={}, response={}", message.getTo(), response);
        } catch (Exception e) {
            log.warn("[Push] Expo push send failed. to={}, reason={}", message.getTo(), e.getMessage(), e);
        }
    }
}
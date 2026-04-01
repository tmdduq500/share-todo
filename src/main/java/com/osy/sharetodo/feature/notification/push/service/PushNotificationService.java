package com.osy.sharetodo.feature.notification.push.service;

import com.osy.sharetodo.feature.device.domain.DeviceToken;
import com.osy.sharetodo.feature.device.repository.DeviceTokenRepository;
import com.osy.sharetodo.feature.event.domain.Event;
import com.osy.sharetodo.feature.notification.push.client.ExpoPushClient;
import com.osy.sharetodo.feature.notification.push.dto.ExpoPushMessage;
import com.osy.sharetodo.feature.person.domain.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final ExpoPushClient expoPushClient;

    public void sendInvitationCreated(Person recipient, Event event, String inviterName) {
        if (recipient == null || recipient.getAccount() == null) {
            return;
        }

        List<DeviceToken> tokens = deviceTokenRepository
                .findAllByAccount_uidAndActiveTrue(recipient.getAccount().getUid());

        if (tokens.isEmpty()) {
            return;
        }

        for (DeviceToken token : tokens) {
            ExpoPushMessage message = ExpoPushMessage.builder()
                    .to(token.getToken())
                    .title("새로운 초대가 도착했어요")
                    .body(inviterName + "님이 일정에 초대했습니다.")
                    .sound("default")
                    .data(Map.of(
                            "type", "INVITATION_CREATED",
                            "eventUid", event.getUid()
                    ))
                    .build();

            expoPushClient.send(message);
        }
    }
}
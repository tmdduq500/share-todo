package com.osy.sharetodo.feature.notification.mail;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingMailSender implements MailPort {
    @Override
    public void send(String to, String subject, String htmlBody) {
        log.info("[MAIL:DRY-RUN] to={}, subject={}, body={}", to, subject, htmlBody);
    }
}

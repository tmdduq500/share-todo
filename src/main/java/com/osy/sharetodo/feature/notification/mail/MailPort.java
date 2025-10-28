package com.osy.sharetodo.feature.notification.mail;

public interface MailPort {
    void send(String to, String subject, String htmlBody);
}

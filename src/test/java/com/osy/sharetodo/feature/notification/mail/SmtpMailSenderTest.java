package com.osy.sharetodo.feature.notification.mail;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.*;

class SmtpMailSenderTest {

    @Test
    void send_calls_javaMailSender() throws Exception {
        JavaMailSender jms = mock(JavaMailSender.class);
        MimeMessage msg = mock(MimeMessage.class);
        when(jms.createMimeMessage()).thenReturn(msg);

        SmtpMailSender sender = new SmtpMailSender(jms, "your@gmail.com");
        sender.send("to@example.com", "제목", "<b>본문</b>");

        verify(jms, times(1)).send(any(MimeMessage.class));
    }
}
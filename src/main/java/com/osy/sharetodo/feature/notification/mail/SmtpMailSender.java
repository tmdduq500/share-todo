package com.osy.sharetodo.feature.notification.mail;

import com.osy.sharetodo.global.exception.ApiException;
import com.osy.sharetodo.global.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@RequiredArgsConstructor
public class SmtpMailSender implements MailPort{

    private final JavaMailSender mailSender;
    private final String from;

    @Override
    public void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // HTML
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "메일 전송에 실패했습니다.");        }
    }
}

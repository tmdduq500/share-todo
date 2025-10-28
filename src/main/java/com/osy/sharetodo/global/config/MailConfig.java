package com.osy.sharetodo.global.config;

import com.osy.sharetodo.feature.notification.mail.LoggingMailSender;
import com.osy.sharetodo.feature.notification.mail.MailPort;
import com.osy.sharetodo.feature.notification.mail.SmtpMailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
@RequiredArgsConstructor
public class MailConfig {

    private final AppProps appProps;

    @Bean
    @ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "true")
    public MailPort smtpMailPort(JavaMailSender javaMailSender) {
        return new SmtpMailSender(javaMailSender, appProps.getMail().getFrom());
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "false", matchIfMissing = true)
    public MailPort loggingMailPort() {
        return new LoggingMailSender();
    }
}
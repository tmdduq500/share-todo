package com.osy.sharetodo.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProps {
    private String frontBaseUrl;
    private String backBaseUrl;
    private Mail mail = new Mail();

    @Getter
    @Setter
    public static class Mail {
        private boolean enabled = false;
        private String from;
    }
}
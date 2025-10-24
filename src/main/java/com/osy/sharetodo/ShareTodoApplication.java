package com.osy.sharetodo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ShareTodoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShareTodoApplication.class, args);
    }

}

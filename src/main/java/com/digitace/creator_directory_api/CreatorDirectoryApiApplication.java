package com.digitace.creator_directory_api;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

import java.util.TimeZone;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class CreatorDirectoryApiApplication {

    @PostConstruct
    public void init() {
        // Forces the JVM to UTC, bypassing the Asia/Calcutta DB rejection
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] args) {
        SpringApplication.run(CreatorDirectoryApiApplication.class, args);
    }
}

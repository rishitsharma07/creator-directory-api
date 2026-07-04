package com.digitace.creator_directory_api;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

import java.util.TimeZone;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class CreatorDirectoryApiApplication {


    public static void main(String[] args) {

        // Force JVM timezone before Spring starts
        System.setProperty("user.timezone", "UTC");
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(CreatorDirectoryApiApplication.class, args);
    }
}

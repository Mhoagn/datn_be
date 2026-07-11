package com.example.demo.config;

import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class TimeZoneConfig {

    private static final Logger log = LoggerFactory.getLogger(TimeZoneConfig.class);

    @Value("${app.timezone:Asia/Ho_Chi_Minh}")
    private String appTimeZone;

    @PostConstruct
    void configureJvmTimeZone() {
        TimeZone zone = TimeZone.getTimeZone(appTimeZone);
        TimeZone.setDefault(zone);
        log.info("[TimeZone] JVM default timezone set to {}", zone.getID());
    }
}

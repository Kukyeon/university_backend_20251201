package com.university.home;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import jakarta.annotation.PostConstruct;

@EnableScheduling
@SpringBootApplication
@EntityScan("com.university.home.entity")
public class University20251201Application {


    @PostConstruct
    public void init() {
        // 애플리케이션 수준에서 타임존 강제 고정
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
	
    public static void main(String[] args) {
        SpringApplication.run(University20251201Application.class, args);
    }
}
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

	public static void main(String[] args) {
		SpringApplication.run(University20251201Application.class, args);
	}
	@PostConstruct
    public void init() {
        // "내 시간은 무조건 서울이다" 선언
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        System.out.println("현재 시간대: " + TimeZone.getDefault().getID());
    }

}

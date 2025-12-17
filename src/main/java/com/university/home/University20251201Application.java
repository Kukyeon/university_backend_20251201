package com.university.home;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EntityScan("com.university.home.entity")
public class University20251201Application {

	public static void main(String[] args) {
		SpringApplication.run(University20251201Application.class, args);
	}

}

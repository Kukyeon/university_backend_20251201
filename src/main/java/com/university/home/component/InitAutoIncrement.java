package com.university.home.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class InitAutoIncrement implements CommandLineRunner {

	@Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("ALTER TABLE student_tb AUTO_INCREMENT = 2025000001");
        jdbcTemplate.execute("ALTER TABLE staff_tb AUTO_INCREMENT = 250001");
        jdbcTemplate.execute("ALTER TABLE professor_tb AUTO_INCREMENT = 25000001");
    }
}

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
    	// AUTO_INCREMENT 초기화
        jdbcTemplate.execute("ALTER TABLE student_tb AUTO_INCREMENT = 2025000001");
        jdbcTemplate.execute("ALTER TABLE staff_tb AUTO_INCREMENT = 250001");
        jdbcTemplate.execute("ALTER TABLE professor_tb AUTO_INCREMENT = 25000001");

        // 대학 더미 데이터
        jdbcTemplate.update("INSERT INTO college_tb (id, name) VALUES (1, '공과대학')");
        jdbcTemplate.update("INSERT INTO college_tb (id, name) VALUES (2, '인문대학')");
        jdbcTemplate.update("INSERT INTO college_tb (id, name) VALUES (3, '사회과학대학')");
        jdbcTemplate.update("INSERT INTO college_tb (id, name) VALUES (4, '상경대학')");

        // 학과 더미 데이터
        jdbcTemplate.update("INSERT INTO department_tb (id, name, college_id) VALUES (101, '컴퓨터공학과', 1)");
        jdbcTemplate.update("INSERT INTO department_tb (id, name, college_id) VALUES (102, '전자공학과', 1)");
        jdbcTemplate.update("INSERT INTO department_tb (id, name, college_id) VALUES (103, '화학공학과', 1)");
        jdbcTemplate.update("INSERT INTO department_tb (id, name, college_id) VALUES (104, '기계공학과', 1)");
        jdbcTemplate.update("INSERT INTO department_tb (id, name, college_id) VALUES (105, '신소재공학과', 1)");

        jdbcTemplate.update("INSERT INTO department_tb (id, name, college_id) VALUES (106, '철학과', 2)");
        jdbcTemplate.update("INSERT INTO department_tb (id, name, college_id) VALUES (107, '국사학과', 2)");
        jdbcTemplate.update("INSERT INTO department_tb (id, name, college_id) VALUES (108, '언어학과', 2)");
        jdbcTemplate.update("INSERT INTO department_tb (id, name, college_id) VALUES (109, '국어국문학과', 2)");
        jdbcTemplate.update("INSERT INTO department_tb (id, name, college_id) VALUES (110, '영어영문학과', 2)");

        jdbcTemplate.update("INSERT INTO department_tb (id, name, college_id) VALUES (111, '심리학과', 3)");
        jdbcTemplate.update("INSERT INTO department_tb (id, name, college_id) VALUES (112, '정치외교학과', 3)");
        jdbcTemplate.update("INSERT INTO department_tb (id, name, college_id) VALUES (113, '사회복지학과', 3)");
        jdbcTemplate.update("INSERT INTO department_tb (id, name, college_id) VALUES (114, '언론정보학과', 3)");
        jdbcTemplate.update("INSERT INTO department_tb (id, name, college_id) VALUES (115, '인류학과', 3)");

        jdbcTemplate.update("INSERT INTO department_tb (id, name, college_id) VALUES (116, '경영학과', 4)");
        jdbcTemplate.update("INSERT INTO department_tb (id, name, college_id) VALUES (117, '경제학과', 4)");
        jdbcTemplate.update("INSERT INTO department_tb (id, name, college_id) VALUES (118, '회계학과', 4)");
        jdbcTemplate.update("INSERT INTO department_tb (id, name, college_id) VALUES (119, '농업경영학과', 4)");
        jdbcTemplate.update("INSERT INTO department_tb (id, name, college_id) VALUES (120, '무역학과', 4)");

        System.out.println("초기 데이터 삽입 완료!");
    }
}

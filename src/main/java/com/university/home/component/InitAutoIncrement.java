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

        // 대학 더미 데이터 (없으면 insert)
        insertIfNotExists(1, "공과대학");
        insertIfNotExists(2, "인문대학");
        insertIfNotExists(3, "사회과학대학");
        insertIfNotExists(4, "상경대학");

        // 학과 더미 데이터 (없으면 insert)
        insertDepartmentIfNotExists(101, "컴퓨터공학과", 1);
        insertDepartmentIfNotExists(102, "전자공학과", 1);
        insertDepartmentIfNotExists(103, "화학공학과", 1);
        insertDepartmentIfNotExists(104, "기계공학과", 1);
        insertDepartmentIfNotExists(105, "신소재공학과", 1);

        insertDepartmentIfNotExists(106, "철학과", 2);
        insertDepartmentIfNotExists(107, "국사학과", 2);
        insertDepartmentIfNotExists(108, "언어학과", 2);
        insertDepartmentIfNotExists(109, "국어국문학과", 2);
        insertDepartmentIfNotExists(110, "영어영문학과", 2);

        insertDepartmentIfNotExists(111, "심리학과", 3);
        insertDepartmentIfNotExists(112, "정치외교학과", 3);
        insertDepartmentIfNotExists(113, "사회복지학과", 3);
        insertDepartmentIfNotExists(114, "언론정보학과", 3);
        insertDepartmentIfNotExists(115, "인류학과", 3);

        insertDepartmentIfNotExists(116, "경영학과", 4);
        insertDepartmentIfNotExists(117, "경제학과", 4);
        insertDepartmentIfNotExists(118, "회계학과", 4);
        insertDepartmentIfNotExists(119, "농업경영학과", 4);
        insertDepartmentIfNotExists(120, "무역학과", 4);

        System.out.println("초기 데이터 삽입 완료!");
    }

    private void insertIfNotExists(int id, String name) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM college_tb WHERE id = ?", Integer.class, id
        );
        if (count == 0) {
            jdbcTemplate.update("INSERT INTO college_tb (id, name) VALUES (?, ?)", id, name);
        }
    }

    private void insertDepartmentIfNotExists(int id, String name, int collegeId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM department_tb WHERE id = ?", Integer.class, id
        );
        if (count == 0) {
            jdbcTemplate.update(
                "INSERT INTO department_tb (id, name, college_id) VALUES (?, ?, ?)",
                id, name, collegeId
            );
        }
    }
}

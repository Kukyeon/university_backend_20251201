package com.university.home.component;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.university.home.dto.StaffDto;
import com.university.home.service.StaffService;

@Component
@Transactional
public class InitAutoIncrement implements CommandLineRunner {

	@Autowired
	@Lazy
	private StaffService staffService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {

        // AUTO_INCREMENT 초기화
        jdbcTemplate.execute("ALTER TABLE student_tb AUTO_INCREMENT = 2025000001");
        jdbcTemplate.execute("ALTER TABLE staff_tb AUTO_INCREMENT = 250001");
        jdbcTemplate.execute("ALTER TABLE professor_tb AUTO_INCREMENT = 25000001");
        jdbcTemplate.execute("ALTER TABLE subject_tb AUTO_INCREMENT = 10000");

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

        insertRoomIfNotExists("E601", 1);
        insertRoomIfNotExists("E602", 1);
        insertRoomIfNotExists("E701", 1);
        insertRoomIfNotExists("E702", 1);
        insertRoomIfNotExists("E801", 1);
        insertRoomIfNotExists("E802", 1);
        insertRoomIfNotExists("E901", 1);
        insertRoomIfNotExists("E902", 1);
        insertRoomIfNotExists("E904", 1);
        insertRoomIfNotExists("E905", 1);

        insertRoomIfNotExists("H101", 2);
        insertRoomIfNotExists("H102", 2);
        insertRoomIfNotExists("H103", 2);
        insertRoomIfNotExists("H104", 2);
        insertRoomIfNotExists("H201", 2);
        insertRoomIfNotExists("H202", 2);
        insertRoomIfNotExists("H203", 2);
        insertRoomIfNotExists("H204", 2);
        insertRoomIfNotExists("H301", 2);
        insertRoomIfNotExists("H302", 2);

        insertRoomIfNotExists("S301", 3);
        insertRoomIfNotExists("S302", 3);
        insertRoomIfNotExists("S303", 3);
        insertRoomIfNotExists("S304", 3);
        insertRoomIfNotExists("S305", 3);
        insertRoomIfNotExists("S306", 3);
        insertRoomIfNotExists("S307", 3);
        insertRoomIfNotExists("S308", 3);
        insertRoomIfNotExists("S309", 3);
        insertRoomIfNotExists("S310", 3);

        insertRoomIfNotExists("C301", 4);
        insertRoomIfNotExists("C302", 4);
        insertRoomIfNotExists("C303", 4);
        insertRoomIfNotExists("C304", 4);
        insertRoomIfNotExists("C305", 4);

        insertQuestionIfNotExists();

        insertTuitionIfNotExists(1, 4868500);
        insertTuitionIfNotExists(2, 3588500);
        insertTuitionIfNotExists(3, 3588500);
        insertTuitionIfNotExists(4, 3588500);
        
        insertScholarshipIfNotExists(1, 5000000); // 성적우수 A유형 (전액 기준 예시)
        insertScholarshipIfNotExists(2, 2547400); // 성적우수 B유형 (반액 기준 예시)

     // 기본 스태프 계정 생성
        insertStaffIfNotExists();

     // 학사일정 (2025-1학기)
        insertScheduleIfNotExists(250001L, "2025-01-27", "2025-02-01", "2025-1학기 예비수강신청");
        insertScheduleIfNotExists(250001L, "2025-02-13", "2025-02-17", "2025-1학기 수강신청");
        insertScheduleIfNotExists(250001L, "2025-02-17", "2025-02-23", "2025-1학기 등록");
        insertScheduleIfNotExists(250001L, "2025-02-22", "2025-02-22", "복학 접수 마감");
        insertScheduleIfNotExists(250001L, "2025-02-26", "2025-02-26", "졸업예배");
        insertScheduleIfNotExists(250001L, "2025-02-27", "2025-02-27", "학위수여식");
        insertScheduleIfNotExists(250001L, "2025-03-01", "2025-03-01", "삼일절");
        insertScheduleIfNotExists(250001L, "2025-03-02", "2025-03-02", "개강/교무위원회");
        insertScheduleIfNotExists(250001L, "2025-03-06", "2025-03-08", "수강신청 확인 및 변경");
        insertScheduleIfNotExists(250001L, "2025-03-10", "2025-03-13", "2025-1학기 추가등록");
        insertScheduleIfNotExists(250001L, "2025-03-13", "2025-03-17", "조기졸업 신청");
        insertScheduleIfNotExists(250001L, "2025-03-15", "2025-03-15", "미등록자 일반 휴학 접수 마감/ 등록금 전액반환 마감");

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
    private void insertRoomIfNotExists(String id, int collegeId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM room_tb WHERE id = ?",
            Integer.class,
            id
        );

        if (count == 0) {
            jdbcTemplate.update(
                "INSERT INTO room_tb (id, college_id) VALUES (?, ?)",
                id, collegeId
            );
        }
    }
    private void insertQuestionIfNotExists() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM question_tb",
            Integer.class
        );

        if (count == 0) {
            jdbcTemplate.update("""
                INSERT INTO question_tb
                (question1, question2, question3, question4, question5, question6, question7, sug_content)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """,
            "강의 내용은 자신에게 학습 욕구를 불러일으킬 만큼 적절한 수준이었는가?",
            "이 강의를 통하여 다른 강의에서는 접할 수 없는 새로운 내용을 배울 수 있었는가?",
            "강의를 통하여 해당 교과목에 대한 이해(실기 능력과 기능)가 개선되었는가?",
            "교수는 주요 주제들간의 관계가 드러나도록 내용을 구조화 하여 전달하였는가?",
            "교수는 열정을 갖고 수업을 진행하였는가?",
            "수업시간은 제대로 이루어졌는가?",
            "강의 내용이 과목명에 부합하는가?",
            "기타"
            );
        }
    }
    private void insertTuitionIfNotExists(int collegeId, int tuition) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM coll_tuit_tb WHERE college_id = ?",
            Integer.class,
            collegeId
        );

        if (count == 0) {
            jdbcTemplate.update(
                "INSERT INTO coll_tuit_tb (college_id, amount) VALUES (?, ?)",
                collegeId, tuition
            );
        }
    }
    private void insertScheduleIfNotExists(
    	    Long staffId,
    	    String startDay,
    	    String endDay,
    	    String information
    	) {
    	    Integer count = jdbcTemplate.queryForObject(
    	        """
    	        SELECT COUNT(*)
    	        FROM schedule_tb
    	        WHERE start_day = ?
    	          AND information = ?
    	        """,
    	        Integer.class,
    	        startDay,
    	        information
    	    );

    	    if (count == 0) {
    	        jdbcTemplate.update(
    	            """
    	            INSERT INTO schedule_tb
    	            (staff_id, start_day, end_day, information)
    	            VALUES (?, ?, ?, ?)
    	            """,
    	            staffId, startDay, endDay, information
    	        );
    	    }
    	}
    private void insertStaffIfNotExists() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM staff_tb WHERE email = ?",
            Integer.class,
            "admin@university.com"
        );

        if (count == 0) {
            StaffDto dto = new StaffDto();
            dto.setName("학사관리자");
            dto.setAddress("대학교 본관");
            dto.setBirthDate(LocalDate.of(1980, 1, 1));
            dto.setEmail("admin@university.com");
            dto.setGender("여성"); // 프로젝트 기준에 맞게
            dto.setTel("010-0000-0000");

            staffService.createStaff(dto);
        }
        
    }
    private void insertScholarshipIfNotExists(int type, int amount) {
        // 이미 해당 타입의 장학금이 있는지 확인
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM scholarship_tb WHERE type = ?",
            Integer.class,
            type
        );

        if (count == 0) {
            jdbcTemplate.update(
                "INSERT INTO scholarship_tb (type, max_amount) VALUES (?, ?)",
                type, amount
            );
        }
    }


}

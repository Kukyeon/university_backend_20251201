package com.university.home.dto;

import com.university.home.entity.Room;
import com.university.home.entity.Subject;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SyllabusDto {
    private Long subjectId;
    private String name;        // 강의명
    private Long subYear;       // 연도
    private Long semester;      // 학기
    private Long grades;        // 학점
    private String type;        // 이수구분
    private String subDay;      // 요일
    private Long startTime;  // 시작시간
    private Long endTime;    // 종료시간
    private String roomId;      // 강의실
    private String collegeName; // 단과대학명
    
    // 교수 정보
    private String deptName;       // 소속 학과
    private String professorName;  // 교수명
    private String tel;            // 연락처
    private String email;          // 이메일
    
    // 상세 내용 (DB에 컬럼이 없다면 임시 텍스트라도 넣어줘야 함)
    private String overview;    // 강의 개요
    private String objective;   // 강의 목표
    private String textbook;    // 교재
    private String program;     // 주간 계획

    // Entity -> DTO 변환 메서드
    public static SyllabusDto fromEntity(Subject subject) {
        return SyllabusDto.builder()
                .subjectId(subject.getId())
                .name(subject.getName())
                .subYear(subject.getSubYear())
                .semester(subject.getSemester())
                .grades(subject.getGrades())
                .type(subject.getType())
                .subDay(subject.getSubDay())
                .startTime(subject.getStartTime())
                .endTime(subject.getEndTime())
                .roomId(subject.getRoom().getId())
                // Null Check 중요!
                .collegeName(subject.getDepartment() != null && subject.getDepartment().getCollege() != null 
                        ? subject.getDepartment().getCollege().getName() : "")
                .deptName(subject.getDepartment() != null ? subject.getDepartment().getName() : "")
                .professorName(subject.getProfessor() != null ? subject.getProfessor().getName() : "미정")
                .email(subject.getProfessor() != null ? subject.getProfessor().getEmail() : "")
                .tel("02-123-4567") // 임시 데이터 (Professor 엔티티에 tel이 없어서)
                
                // 아래 필드들이 Subject 엔티티에 없다면 임시 텍스트 반환
//                .overview("본 강의는 " + subject.getName() + "에 대한 심화 학습을 진행합니다.")
//                .objective("1. 기초 이론 습득\n2. 실무 능력 배양")
//                .textbook("자체 교재")
//                .program("1주차: OT\n2주차: 기초\n8주차: 중간고사\n15주차: 기말고사")
                .build();
    }
}
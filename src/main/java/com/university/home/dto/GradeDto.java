package com.university.home.dto;

import lombok.Data;

@Data
public class GradeDto {
	private Long stuSubId;
	private Long subYear;        // 연도
    private Long semester;       // 학기
    private Long subjectId;         // 과목 ID
    private String subjectName;     // 과목명
    private String majorType;       // 구분 (전공/교양 등)
    private Long credit;         // 학점
    private String grade;           // 최종 등급 (A+, B0...)
    private Long convertedMark;     // 환산점수
    private boolean evaluated;     // 평가 ID (있으면 이미 평가 완료)
}

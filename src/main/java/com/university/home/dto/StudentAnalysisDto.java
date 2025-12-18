package com.university.home.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentAnalysisDto {

	private String name;
	private Double avgGrade;      // 학사 시스템 (Grade)
    private Integer absenceCount; // 전자출결 시스템 (Attendance - 추가 필요)
    private Integer lmsLoginCount;// LMS 시스템 (LoginLog - 추가 필요)
    private Integer counselingCount; // 상담 이력 (Counseling)
    
 // AI에게 보낼 프롬프트용 텍스트로 변환
    public String toPromptString() {
        return String.format("""
            - 이름: %s
            - 평균학점: %.2f점
            - 결석횟수: %d회
            - LMS접속: %d회
            - 상담이력: %d회
            """, name, avgGrade, absenceCount, lmsLoginCount, counselingCount);
    }
}

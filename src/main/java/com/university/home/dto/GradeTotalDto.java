package com.university.home.dto;

import lombok.Data;

@Data
public class GradeTotalDto {
    private Long subYear;         // 연도
    private Long semester;        // 학기
    private Long totalCredit;     // 신청학점 합계
    private Long earnedCredit;    // 취득학점 합계
    private Double averageScore;  // 평점평균
}

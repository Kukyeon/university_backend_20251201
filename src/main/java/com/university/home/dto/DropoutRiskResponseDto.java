package com.university.home.dto;

import java.time.LocalDate;

import com.university.home.entity.DropoutRisk;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DropoutRiskResponseDto {
	//학생 기본 정보
	private Long studentId;
	private String studentName;
	private String departmentName; //학과
	private Long grade; //학년
	
	//AI분석 결과
	private Double riskScore;  //위험 점수
	private String riskLevel;  //위험/주의/정상
	private String reason;  //AI분석 원인
	private LocalDate analyzedDate; //분석 날짜

	//Entity -> Dto 변환 메소드
	public static DropoutRiskResponseDto fromEntity(DropoutRisk risk) {
		return DropoutRiskResponseDto.builder()
				.studentId(risk.getStudent().getId())
                .studentName(risk.getStudent().getName())
                .departmentName(risk.getStudent().getDepartment().getName())
                .grade(risk.getStudent().getGrade())
                .riskScore(risk.getRiskScore())
                .riskLevel(risk.getRiskLevel())
                .reason(risk.getReason())
                .analyzedDate(risk.getAnalyzedDate())
                .build();
	}

}

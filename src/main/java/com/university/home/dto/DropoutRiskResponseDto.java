package com.university.home.dto;

import java.time.LocalDate;

import com.university.home.entity.DropoutRisk;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DropoutRiskResponseDto {
	private Long id;
	//학생 기본 정보
	private Long studentId;
	private String studentName;
	private String departmentName; 
	private Long grade;
	
	//AI분석 결과
	private Double riskScore; 
	private String riskLevel; 
	private String reason; 
	private LocalDate analyzedDate; 

	//Entity -> Dto 변환 메소드
	public static DropoutRiskResponseDto fromEntity(DropoutRisk risk) {
		return DropoutRiskResponseDto.builder()
				.id(risk.getId())
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

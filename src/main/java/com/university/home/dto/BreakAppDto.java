package com.university.home.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BreakAppDto {

	private Long studentId;
	
	private Long studentGrade;
	
	private Long fromYear;
	
	private Long fromSemester;
	
	@NotNull
	private Long toYear;
	
	@NotNull
	private Long toSemester;
	
	@NotBlank(message = "휴학 사유를 선택해주세요")
	private String type;
}

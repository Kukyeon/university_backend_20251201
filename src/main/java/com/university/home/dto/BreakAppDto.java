package com.university.home.dto;


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
	
	@NotNull
	private String type;
}

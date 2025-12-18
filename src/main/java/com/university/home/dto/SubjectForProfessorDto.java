package com.university.home.dto;

import lombok.Data;

@Data
public class SubjectForProfessorDto {

	private Long id;
	private String name;
	private String subDay;
	private Long startTime;
	private Long endTime;
	private String roomId;

	private Long subYear;
	private Long semester;
}

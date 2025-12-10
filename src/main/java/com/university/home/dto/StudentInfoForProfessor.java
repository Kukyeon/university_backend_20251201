package com.university.home.dto;

import lombok.Data;

@Data
public class StudentInfoForProfessor {

	private Long id;
	
	private Long studentId;
	
	private String studentName;
	// 학생 소속
	private String deptName;
	// 결석 횟수
	private Long absent;
	// 지각 횟수
	private Long lateness;
	// 과제 점수
	private Long homework;
	// 중간고사 점수
	private Long midExam;
	// 기말고사 점수
	private Long finalExam;
	// 총합 환산 점수
	private Long convertedMark;
	
}

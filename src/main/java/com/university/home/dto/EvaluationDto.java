package com.university.home.dto;

import lombok.Data;

@Data
public class EvaluationDto {
	private Long stuSubId;
    private Long answer1;
    private Long answer2;
    private Long answer3;
    private Long answer4;
    private Long answer5;
    private Long answer6;
    private Long answer7;
    private String improvements;
    private Long studentId;
    private Long professorId;
    private Long subjectId;
    private String studentName;
    private String subjectName;
    private Double avgScore;
}

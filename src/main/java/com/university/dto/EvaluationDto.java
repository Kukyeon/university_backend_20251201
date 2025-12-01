package com.university.dto;

import lombok.Data;

@Data
public class EvaluationDto {
    private Long studentId;
    private Long subjectId;
    private Long answer1;
    private Long answer2;
    private Long answer3;
    private Long answer4;
    private Long answer5;
    private Long answer6;
    private Long answer7;
    private String improvements;
}

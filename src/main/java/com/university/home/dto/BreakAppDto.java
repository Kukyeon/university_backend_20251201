package com.university.home.dto;


import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BreakAppDto {
	private Long id;
    private String type;
    private String status;
    private Long fromYear;
    private Long fromSemester;
    private Long toYear;
    private Long toSemester;
    private LocalDate appDate;

    private Long studentId;
    private String studentName;                   // 필요시 복사
    private String departmentName;
    private String collegeName;
}

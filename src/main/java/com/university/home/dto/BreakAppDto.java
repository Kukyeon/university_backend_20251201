package com.university.home.dto;
import java.time.LocalDate;
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
    private String studentName;
    private String departmentName;
    private String collegeName;
}

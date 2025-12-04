package com.university.home.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StuSchDto {

	private Long id;                 // stu_sch_tb PK
    private Long studentId;          // 학생 ID
    private Long schYear;            // 학년/연도
    private Long semester;           // 학기
    private Long scholarshipTypeId;  // 장학금 유형 ID
    private Long scholarshipMaxAmount; // 장학금 최대 금액
}

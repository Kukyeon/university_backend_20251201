package com.university.home.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class StudentStatDto {

	private Long id;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String status;
    private Long breakAppId; // 연관된 휴학 신청 ID만 저장
}

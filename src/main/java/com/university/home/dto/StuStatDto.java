package com.university.home.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class StuStatDto {
    private Long id;
    private String status;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Long breakAppId;
}
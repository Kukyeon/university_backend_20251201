package com.university.home.dto;

import lombok.Data;

@Data
public class RecordSearchRequestDto {
    private String studentName;
    private String consultationDate; // YYYY-MM-DD
    private String keyword;
}
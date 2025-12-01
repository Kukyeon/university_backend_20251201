package com.university.home.dto;

import lombok.Data;

@Data
public class NoticePageFormDto {
    private Integer page;
    private String keyword;
    private String type;
}

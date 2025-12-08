package com.university.home.dto;

import java.time.LocalDateTime;
import org.springframework.web.multipart.MultipartFile;

import com.university.home.utils.LocalDateTimeUtil;

import lombok.Data;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Data
public class NoticeFormDto {

    private Long id;
    private String category;

    @NotEmpty
    @Size(max = 50)
    private String title;

    @NotEmpty
    private String content;

    private Long views;
    private LocalDateTime createdTime;

    private String imageUrl;
    private MultipartFile file;

    // 시간 포맷 처리
    public String timeFormat() {
        return LocalDateTimeUtil.formatDateTime(createdTime);
    }

    public String dateFormat() {
        return LocalDateTimeUtil.formatDate(createdTime);
    }
}

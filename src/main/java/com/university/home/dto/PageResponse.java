package com.university.home.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Data;

@Data
public class PageResponse<T> {
 private List<T> content;   // 실제 데이터
    private int page;          // 현재 페이지
    private int size;          // 페이지당 요소 수
    private long totalElements;// 총 요소 수
    private int totalPages;    // 총 페이지 수
    
    public PageResponse(Page<T> pageData) {
        this.content = pageData.getContent();
        this.page = pageData.getNumber();
        this.size = pageData.getSize();
        this.totalElements = pageData.getTotalElements();
        this.totalPages = pageData.getTotalPages();
    }
}

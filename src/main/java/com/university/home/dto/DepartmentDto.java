package com.university.home.dto;

import lombok.Data;

@Data
public class DepartmentDto {
	private Long id;
    private String name;
    private CollegeDto college;  // 중첩
}

package com.university.home.dto;

import lombok.Data;

@Data
public class RoomDto {
	private String id;       // 강의실 호수
    private CollegeDto college;  // 중첩
}

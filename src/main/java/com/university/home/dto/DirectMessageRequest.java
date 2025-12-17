package com.university.home.dto;

import lombok.Data;

@Data
public class DirectMessageRequest {

	private Long targetStudentId;
	private String content;

}

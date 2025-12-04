package com.university.home.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserPwDto {

	private Long userId; // 로그인 중인 사용자 아이디
	@NotBlank
	private String oldPassword; // 현재 비밀번호 확인용
	@NotBlank
	private String newPassword;
}

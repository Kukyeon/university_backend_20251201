package com.university.home.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserPwDto {

	private Long userId; // 로그인 중인 사용자 아이디
	@NotBlank(message = "기존 비밀번호를 입력해주세요.")
	private String oldPassword; // 현재 비밀번호 확인용
	@NotBlank(message = "새로운 비밀번호를 입력해주세요.")
	@Size(min = 6, max = 20, message = "패스워드는 6~20자 사이여야합니다.")
	private String newPassword;
}

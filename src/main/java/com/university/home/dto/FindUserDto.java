package com.university.home.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FindUserDto {
	 @NotBlank(message = "이름을 입력해주세요.")
	private String name;
	
	@Email(message = "유효한 이메일을 입력해주세요.")
	private String email;
	
	@NotBlank(message = "역할을 선택해주세요. (student, professor, staff)")
	private String userRole;
	
	// 비밀번호 찾기 시 필요
	private Long id; // Optional: ID를 알고 있어야 pw 찾기 가능
}

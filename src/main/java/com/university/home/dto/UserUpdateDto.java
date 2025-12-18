package com.university.home.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserUpdateDto {

	private Long id;
	@NotBlank(message = "주소를 입력해주세요")
	private String address;
	@NotBlank(message = "전화번호를 입력해주세요")
	private String tel;
	@NotBlank(message = "이메일을 입력해주세요.")
	@Email(message = "이메일 형식이 아닙니다.")
	private String email;
	
}

package com.university.home.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UserUpdateDto {

	private Long id;
	@NotEmpty
	private String address;
	@NotBlank
	private String tel;
	@Email
	private String email;
	
}

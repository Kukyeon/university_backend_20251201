package com.university.home.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Lombok;

@Data
public class UserDto {
	@Min(100000)
	@Max(2147483646)
	private Long id;
	@Size(min = 6, max = 20, message = "패스워드는 6~20자 사이여야합니다.")
	private String password;
	private String rememberId;
}

package com.university.home.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfessorDto {

	private Long id;

	@NotEmpty(message = "이름을 입력해주세요")
	private String name;
	@NotNull(message = "생일을 입력해주세요")
	private LocalDate birthDate;
	@NotEmpty(message = "성별을 선택해주세요")
	private String gender;
	
	@NotEmpty(message = "주소를 입력해주세요")
	private String address;
	
	@Size(min = 11, max = 13 ,  message = "11자리에서 13자리 숫자로 입력해주세요")
	private String tel;
	
	private DepartmentDto department;
	
	@Email
	@NotEmpty(message = "이메일을 입력해주세요")
	private String email;
	
	private LocalDate hireDate;
}

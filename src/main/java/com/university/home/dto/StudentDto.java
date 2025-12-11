package com.university.home.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StudentDto {

	private Long id;

	@NotEmpty
	@Size(min = 2, max = 30)
	private String name;
	
	private LocalDate birthDate;
	
	private String gender;
	
	@NotEmpty
	private String address;
	
	@Size(min = 11, max = 13)
	private String tel;
	
	//private Department department;
	private DepartmentDto department;
	
	private LocalDate entranceDate;
	@Email
	private String email;
	
	private StuStatDto currentStatus;
	
	private Long grade;
	
	private Long semester;

}

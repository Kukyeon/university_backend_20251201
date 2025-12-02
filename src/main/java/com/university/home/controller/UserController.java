package com.university.home.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.university.home.dto.ProfessorDto;
import com.university.home.dto.StaffDto;
import com.university.home.dto.StudentDto;
import com.university.home.exception.CustomRestfullException;
import com.university.home.service.ProfessorService;
import com.university.home.service.StaffService;
import com.university.home.service.StudentService;
import com.university.home.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/api/user")
public class UserController {

	@Autowired
	UserService userService;
	@Autowired
	ProfessorService professorService;
	@Autowired
	StudentService studentService;
	@Autowired
	StaffService staffService;
	
	@PostMapping("/staff")
	public ResponseEntity<?> createStaff(@Valid@RequestBody StaffDto dto, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			StringBuilder sb = new StringBuilder();
			bindingResult.getAllErrors().forEach(error -> {
				sb.append(error.getDefaultMessage()).append("\\n");
			});
			throw new CustomRestfullException(sb.toString(), HttpStatus.BAD_REQUEST);
		}
		Long staffId = staffService.createStaff(dto);
		
		return ResponseEntity.ok(staffId);
	}
	@PostMapping("/student")
	public ResponseEntity<?> createStudent(@Valid@RequestBody StudentDto dto, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			StringBuilder sb = new StringBuilder();
			bindingResult.getAllErrors().forEach(error -> {
				sb.append(error.getDefaultMessage()).append("\\n");
			});
			throw new CustomRestfullException(sb.toString(), HttpStatus.BAD_REQUEST);
		}
		Long studentId = studentService.createStudent(dto);
		
		return ResponseEntity.ok(studentId);
	}
	@PostMapping("/professor")
	public ResponseEntity<?> createProffesor(@Valid@RequestBody ProfessorDto dto, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			StringBuilder sb = new StringBuilder();
			bindingResult.getAllErrors().forEach(error -> {
				sb.append(error.getDefaultMessage()).append("\\n");
			});
			throw new CustomRestfullException(sb.toString(), HttpStatus.BAD_REQUEST);
		}
		Long professorId = professorService.createProfessor(dto);
		
		return ResponseEntity.ok(professorId);
	}
	
}

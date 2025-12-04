package com.university.home.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.university.home.dto.UserDto;
import com.university.home.entity.Staff;
import com.university.home.entity.User;
import com.university.home.exception.CustomRestfullException;
import com.university.home.service.ProfessorService;
import com.university.home.service.StaffService;
import com.university.home.service.StudentService;
import com.university.home.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class PersonalController {

	@Autowired
	ProfessorService professorService;
	@Autowired
	StaffService staffService;
	@Autowired
	StudentService studentService;
	@Autowired
	UserService userService;
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody @Valid UserDto dto, BindingResult bindingResult) {
	    if (bindingResult.hasErrors()) {
	        StringBuilder sb = new StringBuilder();
	        bindingResult.getAllErrors().forEach(error -> sb.append(error.getDefaultMessage()).append("\n"));
	        throw new CustomRestfullException(sb.toString(), HttpStatus.BAD_REQUEST);
	    }

	    // 1️⃣ User 검증
	    User user = userService.login(dto.getId(), dto.getPassword());

	    // 2️⃣ 역할(role)에 따라 실제 엔티티 조회
	    Object result;
	    switch(user.getUserRole()) {
	        case "student":
	            result = studentService.readStudent(dto.getId());
	            break;
	        case "professor":
	            result = professorService.readProfessor(dto.getId());
	            break;
	        case "staff":
	            result = staffService.readStaff(dto.getId());
	            break;
	        default:
	            throw new CustomRestfullException("Unknown user role", HttpStatus.BAD_REQUEST);
	    }

	    // 3️⃣ 반환
	    return ResponseEntity.ok(result);
	}


}

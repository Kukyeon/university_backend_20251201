package com.university.home.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.university.home.dto.PageResponse;
import com.university.home.dto.ProfessorDto;
import com.university.home.dto.StaffDto;
import com.university.home.dto.StudentDto;
import com.university.home.entity.Professor;
import com.university.home.entity.Student;
import com.university.home.exception.CustomRestfullException;
import com.university.home.service.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController /*~~(Could not parse as Java)~~>*/{


	@Autowired
	UserService userService;
	@Autowired
	ProfessorService professorService;
	@Autowired
	StudentService studentService;
	@Autowired
	StaffService staffService;
	
	@PostMapping("/staff")
	public ResponseEntity<?> createStaff(@Valid @RequestBody StaffDto dto, BindingResult bindingResult) {
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
		Student student = studentService.createStudentWithStatus(dto);
		
		return ResponseEntity.ok(student);
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
	@GetMapping("/list/professor")
	public ResponseEntity<?> professorList(
			 @RequestParam(name = "professorId", required = false) Long professorId,
		        @RequestParam(name = "deptId", required = false) Long deptId,
		        @RequestParam(name = "page", defaultValue = "0") int page,
		        @RequestParam(name = "size", defaultValue = "20") int size) {

	    // 특정 교수 조회
	    if (professorId != null) {
	        return ResponseEntity.ok(
	                professorService.getProfessorById(professorId));
	    }
	    Page<Professor> pageData;
	    if (deptId != null) {
	        pageData = professorService.getProfessorsByDep(deptId, page, size);
	    } else {
	        pageData = professorService.getProfessors(page, size);
	    }

	    return ResponseEntity.ok(new PageResponse<>(pageData));
	}
	@GetMapping("/list/student")
	public ResponseEntity<?> studentList(
	        @RequestParam(name = "studentId", required = false) Long studentId,
	        @RequestParam(name = "deptId", required = false) Long deptId,
	        @RequestParam(name = "page", defaultValue = "0") int page,
	        @RequestParam(name = "size", defaultValue = "20") int size) {

	    // 특정 교수 조회
	    if (studentId != null) {
	        return ResponseEntity.ok(
	                studentService.getStudentById(studentId));
	    }
	    Page<Student> pageData;
	    // 학과별 조회
	    if (deptId != null) {
	    	pageData = studentService.getStudentsByDep(deptId, page, size);
	    } else {
	    	pageData = studentService.getStudents(page, size);
		}
	    return ResponseEntity.ok(new PageResponse<>(pageData));
	}
	@GetMapping("/list/student/update")
	public ResponseEntity<?> updateStudentGradeAndSemester() {
		int updateCount = studentService.updateStudentGradeAndSemesters();
		return ResponseEntity.ok("업데이트 완료: " +  updateCount + "명");
	}
}

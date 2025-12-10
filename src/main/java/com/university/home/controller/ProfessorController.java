package com.university.home.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.university.home.dto.StudentInfoForProfessor;
import com.university.home.dto.SubjectForProfessorDto;
import com.university.home.service.CustomUserDetails;
import com.university.home.service.ProfessorService;

@RestController
@RequestMapping("/api/prof")
public class ProfessorController {

	@Autowired
	ProfessorService professorService;
	
	@GetMapping
	public ResponseEntity<?> subjectList(@AuthenticationPrincipal CustomUserDetails loginUser, @RequestParam(name = "subYear",required = false) Long subYear,@RequestParam(name = "semester",required = false) Long semester) {
		Long professorId = loginUser.getUser().getId();
		List<SubjectForProfessorDto> subjectList = professorService.selectSubjectsByProfessor(professorId, subYear, semester);
		System.out.println("로그인 교수 ID: " + professorId);
		System.out.println("조회된 과목 수: " + subjectList.size());
		System.out.println(subYear);
		return ResponseEntity.ok(subjectList);
	}
	@GetMapping("/student/{subjectId}")
	public ResponseEntity<?> studentList( @PathVariable(name = "subjectId") Long subjectId) {
		 System.out.println("subjectId: " + subjectId);
		List<StudentInfoForProfessor> studentList = professorService.selectStudentBySubject(subjectId);
		
		return ResponseEntity.ok(studentList);
	}
}

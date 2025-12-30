package com.university.home.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.university.home.dto.StudentInfoForProfessor;
import com.university.home.dto.SubjectForProfessorDto;
import com.university.home.dto.SyllabusDto;
import com.university.home.service.CustomUserDetails;
import com.university.home.service.ProfessorService;
import com.university.home.service.SubjectService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/prof")
public class ProfessorController {

	@Autowired
	ProfessorService professorService;
	@Autowired
    SubjectService subjectService;
	
	// 내 강의목록
	@GetMapping
	public ResponseEntity<?> subjectList(@AuthenticationPrincipal CustomUserDetails loginUser, @RequestParam(name = "subYear",required = false) Long subYear,@RequestParam(name = "semester",required = false) Long semester) {
		Long professorId = loginUser.getUser().getId();
		List<SubjectForProfessorDto> subjectList = professorService.selectSubjectsByProfessor(professorId, subYear, semester);
		return ResponseEntity.ok(subjectList);
	}
	// 내강의 수강학생 목록 
	@GetMapping("/student/{subjectId}")
	public ResponseEntity<?> studentList( @PathVariable(name = "subjectId") Long subjectId) {
		List<StudentInfoForProfessor> studentList = professorService.selectStudentBySubject(subjectId);
		
		return ResponseEntity.ok(studentList);
	}
	// 학생 성적 기입
	@PutMapping("/student/{stuSubId}")
	public ResponseEntity<?> updateGrade(
	        @PathVariable(name = "stuSubId") Long stuSubId,
	        @RequestBody @Valid StudentInfoForProfessor dto) {
	   
	   return ResponseEntity.ok(professorService.updateStudentGrade(stuSubId, dto));
	}
	// 내 학과의 교수
	@GetMapping("/my-department")
	public ResponseEntity<?> myDepartmentProfessors(
	        @AuthenticationPrincipal CustomUserDetails loginUser) {

	    Long studentId = loginUser.getUser().getId();
	    return ResponseEntity.ok(
	        professorService.getProfessorsByStudentDepartment(studentId)
	    );
	}
	// 강의계획서 수정
    @PutMapping("/subject/{subjectId}/syllabus")
    public ResponseEntity<?> updateSyllabus(
            @PathVariable("subjectId") Long subjectId,
            @RequestBody SyllabusDto syllabusDto,
            @AuthenticationPrincipal CustomUserDetails loginUser 
    ) {
        Long professorId = loginUser.getUser().getId();

        try {
            subjectService.updateSyllabus(subjectId, professorId, syllabusDto);
            
            return ResponseEntity.ok("강의계획서가 수정되었습니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
            
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류: " + e.getMessage());
        }
    }

}

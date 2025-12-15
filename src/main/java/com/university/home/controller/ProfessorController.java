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

@RestController
@RequestMapping("/api/prof")
public class ProfessorController {

	@Autowired
	ProfessorService professorService;
	@Autowired // SubjectService 주입 추가
    SubjectService subjectService;
	
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
	@PutMapping("/student/{stuSubId}")
	public ResponseEntity<?> updateGrade(
	        @PathVariable(name = "stuSubId") Long stuSubId,
	        @RequestBody StudentInfoForProfessor dto) {
	   
	   return ResponseEntity.ok(professorService.updateStudentGrade(stuSubId, dto));
	}
	
	// ★ [수정 완료된 부분] 강의계획서 수정
    @PutMapping("/subject/{subjectId}/syllabus") // URL 경로 수정 (중복 경로 정리)
    public ResponseEntity<?> updateSyllabus(
            @PathVariable("subjectId") Long subjectId,
            @RequestBody SyllabusDto syllabusDto,
            @AuthenticationPrincipal CustomUserDetails loginUser 
    ) {
        // 1. 로그인한 교수님의 ID 가져오기
        Long professorId = loginUser.getUser().getId();

        try {
            // 2. 서비스 호출 (DTO 전체를 넘겨야 함)
            // syllabusDto 안에 overview, objective 등의 내용이 들어있음
            subjectService.updateSyllabus(subjectId, professorId, syllabusDto);
            
            return ResponseEntity.ok("강의계획서가 수정되었습니다.");

        } catch (IllegalArgumentException e) {
            // 과목이 없을 때
            return ResponseEntity.badRequest().body(e.getMessage());
            
        } catch (SecurityException e) {
            // 3. 본인 과목이 아닐 때 403 Forbidden 반환
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            
        } catch (Exception e) {
            // 기타 서버 에러
            return ResponseEntity.internalServerError().body("서버 오류: " + e.getMessage());
        }
    }
}

package com.university.home.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.university.home.dto.GradeDto;
import com.university.home.dto.GradeTotalDto;
import com.university.home.service.CustomUserDetails;
import com.university.home.service.StuSubService;

@RestController
@RequestMapping("/api/grade")
public class GradeController {

	@Autowired
	StuSubService stuSubService;
	
	@GetMapping("/thisSemester")
    public ResponseEntity<?> getThisSemesterGrades(
            @AuthenticationPrincipal CustomUserDetails loginUser
           
    ) {
		Long studentId = loginUser.getUser().getId();
		 List<GradeDto> list = stuSubService.getThisSemesterGrades(studentId);

		    Map<String, Object> result = new HashMap<>();
		    result.put("gradeList", list);       
		    result.put("submitted", !list.isEmpty()); 
		    return ResponseEntity.ok(result);
    }
	@GetMapping("/semester")
	public ResponseEntity<?> getSemesterGrades(
	        @AuthenticationPrincipal CustomUserDetails loginUser,
	        @RequestParam(name = "year") Long year,
	        @RequestParam(name = "semester") Long semester,
	        @RequestParam(name = "type", required = false) String type
	) {
	    Long studentId = loginUser.getUser().getId();

	    List<GradeDto> list = stuSubService.getGradeBySemester(studentId, year, semester, type);

	    Map<String, Object> result = new HashMap<>();
	    result.put("gradeList", list);
	    result.put("submitted", !list.isEmpty());

	    return ResponseEntity.ok(result);
	}
	 // 전체 누계 성적 조회
	@GetMapping("/total")
	public ResponseEntity<?> getTotalGrades(@AuthenticationPrincipal CustomUserDetails loginUser) {
	    Long studentId = loginUser.getUser().getId();
	    List<GradeTotalDto> totalGrades = stuSubService.readGradeInquiryList(studentId);

	    Map<String, Object> result = new HashMap<>();
	    result.put("gradeList", totalGrades);
	    result.put("submitted", !totalGrades.isEmpty());
	    return ResponseEntity.ok(result);
	}
	// 학생의 성적 있는 년도 불러오기
	@GetMapping("/available-years")
    public ResponseEntity<List<Long>> getAvailableYears(@AuthenticationPrincipal CustomUserDetails loginUser) {
        Long studentId = loginUser.getUser().getId();
        
        List<Long> years = stuSubService.getTakenYears(studentId);
        
        return ResponseEntity.ok(years);
    }
}

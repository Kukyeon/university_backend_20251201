package com.university.home.controller;

import com.university.home.service.CourseService;
import com.university.home.service.CustomUserDetails;
import com.university.home.dto.SyllabusDto;
import com.university.home.entity.PreStuSub;
import com.university.home.entity.Subject;
import com.university.home.repository.PreStuSubRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final PreStuSubRepository preStuSubRepository;
    // 강의 목록 조회 (학기 자동 감지)
    @GetMapping("/list")
    public ResponseEntity<Page<Subject>> getCourseList(
    		@AuthenticationPrincipal CustomUserDetails loginUser,
            @RequestParam(name = "page", defaultValue = "0") int page,        
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "deptId", required = false) Long deptId,
            @RequestParam(name = "targetGrade", required = false) Long targetGrade
    ) {
    	if (loginUser == null) return ResponseEntity.status(401).build();

    	Long studentId = loginUser.getUser().getId();
        return ResponseEntity.ok(courseService.getAvailableCourses(studentId, page, type, name, deptId, targetGrade));
    }
    
    // 모든 강좌 조회 (년도/학기 무관)
    @GetMapping
    public ResponseEntity<Page<Subject>> getAllCourseList(
    		@RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "deptId", required = false) Long deptId,
            @RequestParam(name = "year", required = false) Long year, 
            @RequestParam(name = "semester", required = false) Long semester 
    ) {
        return ResponseEntity.ok(courseService.getAllCourses(page, type, name, deptId, year, semester));
    }

    // 내 수강 내역 조회
    @GetMapping("/history")
    public ResponseEntity<List<?>> getMyHistory(@AuthenticationPrincipal CustomUserDetails loginUser) {
        
        if (loginUser == null) return ResponseEntity.status(401).build();
        
        return ResponseEntity.ok(courseService.getMyCourseHistory(loginUser.getUser().getId()));
    }

    // AI 강의 추천
    @GetMapping("/recommend")
    public ResponseEntity<Map<String, String>> recommendCourses(@AuthenticationPrincipal CustomUserDetails loginUser) {
        if (loginUser == null) return ResponseEntity.status(401).build();
        
        String recommendation = courseService.recommendCourses(loginUser.getUser().getId());
        return ResponseEntity.ok(Map.of("result", recommendation));
    }

    // 수강신청
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @AuthenticationPrincipal CustomUserDetails loginUser,
            @RequestParam("subjectId") Long subjectId) { // ★ 변경됨
        
        if (loginUser == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

        try {
            Long studentId = loginUser.getUser().getId();
            // request.get("subjectId") 할 필요 없이 바로 subjectId 사용 가능
            
            courseService.enroll(studentId, subjectId);
            return ResponseEntity.ok("수강신청 성공!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // 장바구니 목록 조회 (기간 상관없이 확인용)
    @GetMapping("/basket")
    public ResponseEntity<List<PreStuSub>> getMyBasket(@AuthenticationPrincipal CustomUserDetails loginUser) {
        if (loginUser == null) return ResponseEntity.status(401).build();
        
        return ResponseEntity.ok(preStuSubRepository.findByStudentId(loginUser.getUser().getId()));
    }

    // 수강취소
    @DeleteMapping("/cancel")
    public ResponseEntity<String> cancel(
            @AuthenticationPrincipal CustomUserDetails loginUser,
            @RequestParam("subjectId") Long subjectId) {
        
        if (loginUser == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

        try {
            Long studentId = loginUser.getUser().getId();
            
            courseService.cancel(studentId, subjectId);
            return ResponseEntity.ok("수강신청 취소 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body( e.getMessage());
        }
    }
    
    // 강의 상세 조회 (강의계획서용)
    @GetMapping("/syllabus/{subjectId}")
    public ResponseEntity<SyllabusDto> getSyllabus(@PathVariable("subjectId") Long subjectId) {
        // 기존 Repository 활용
        return courseService.Syllabus(subjectId);
    }
}
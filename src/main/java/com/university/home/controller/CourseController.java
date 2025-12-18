package com.university.home.controller;

import com.university.home.service.CourseService;
import com.university.home.service.CustomUserDetails; // 패키지명 확인 필요
import com.university.home.dto.SyllabusDto;
import com.university.home.entity.PreStuSub;
import com.university.home.entity.StuSub;
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
    // ============================ 조회 API ============================

    // 1. 강의 목록 조회 (학기 자동 감지)
    // GET /api/course/list
    @GetMapping("/list")
    public ResponseEntity<Page<Subject>> getCourseList(
            @RequestParam(name = "year", required = false) Long year,
            @RequestParam(name = "semester", required = false) Long semester,
            @RequestParam(name = "page", defaultValue = "0") int page,        
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "deptId", required = false) Long deptId
    ) {
        return ResponseEntity.ok(courseService.getAvailableCourses(year, semester, page, type, name, deptId));
    }
    
 // [추가] 모든 강좌 조회 (년도/학기 무관)
    // GET /api/course/all
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

    // 2. 내 수강 내역 조회
    // GET /api/course/history
    @GetMapping("/history")
    public ResponseEntity<List<?>> getMyHistory(@AuthenticationPrincipal CustomUserDetails loginUser) {
        
        if (loginUser == null) return ResponseEntity.status(401).build();
        
        // Service에서 기간(0, 1)에 따라 List<PreStuSub> 또는 List<StuSub>를 줍니다.
        // 이를 유연하게 받기 위해 와일드카드(?)를 사용합니다.
        return ResponseEntity.ok(courseService.getMyCourseHistory(loginUser.getUser().getId()));
    }

    // 3. AI 강의 추천
    // GET /api/course/recommend
    @GetMapping("/recommend")
    public ResponseEntity<Map<String, String>> recommendCourses(@AuthenticationPrincipal CustomUserDetails loginUser) {
        if (loginUser == null) return ResponseEntity.status(401).build();
        
        String recommendation = courseService.recommendCourses(loginUser.getUser().getId());
        return ResponseEntity.ok(Map.of("result", recommendation));
    }

    // ============================ 동작 API (수강신청) ============================

    // 4. 수강신청
    // POST /api/course/register
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
    
 // 장바구니 목록 무조건 조회 (기간 상관없이 확인용)
    // GET /api/course/basket
    @GetMapping("/basket")
    public ResponseEntity<List<PreStuSub>> getMyBasket(@AuthenticationPrincipal CustomUserDetails loginUser) {
        if (loginUser == null) return ResponseEntity.status(401).build();
        
        // Repository 직접 호출해서 장바구니(PreStuSub) 내용만 가져옴
        return ResponseEntity.ok(preStuSubRepository.findByStudentId(loginUser.getUser().getId()));
    }

    // 5. 수강취소
    // DELETE /api/course/cancel?subjectId=101
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
    
 // 6. 강의 상세 조회 (강의계획서용)
    // GET /api/course/syllabus/101
    @GetMapping("/syllabus/{subjectId}")
    public ResponseEntity<SyllabusDto> getSyllabus(@PathVariable("subjectId") Long subjectId) {
        // 기존 Repository 활용
        return courseService.Syllabus(subjectId);
    }
}
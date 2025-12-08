package com.university.home.controller;

import com.university.home.service.CourseService;
import com.university.home.service.CustomUserDetails; // 패키지명 확인 필요
import com.university.home.entity.StuSub;
import com.university.home.entity.Subject;

import lombok.RequiredArgsConstructor;
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

    // ============================ 조회 API ============================

    // 1. 강의 목록 조회 (학기 자동 감지)
    // GET /api/course/list
    @GetMapping("/list")
    public ResponseEntity<List<Subject>> getCourseList(
            @RequestParam(name = "year", required = false) Long year,
            @RequestParam(name = "semester", required = false) Long semester
    ) {
        return ResponseEntity.ok(courseService.getAvailableCourses(year, semester));
    }

    // 2. 내 수강 내역 조회
    // GET /api/course/history
    @GetMapping("/history")
    public ResponseEntity<List<StuSub>> getMyHistory(@AuthenticationPrincipal CustomUserDetails loginUser) {
        if (loginUser == null) return ResponseEntity.status(401).build();
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
            @RequestBody Map<String, Long> request) {
        
        if (loginUser == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

        try {
            Long studentId = loginUser.getUser().getId();
            Long subjectId = request.get("subjectId");

            courseService.enroll(studentId, subjectId);
            return ResponseEntity.ok("✅ 수강신청 성공!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ 실패: " + e.getMessage());
        }
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
            return ResponseEntity.ok("🗑️ 수강취소 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ 취소 실패: " + e.getMessage());
        }
    }
}
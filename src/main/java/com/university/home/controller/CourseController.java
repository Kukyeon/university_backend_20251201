package com.university.home.controller;

import com.university.home.entity.StuSub;
import com.university.home.entity.Subject;
import com.university.home.service.CourseRecommendationService;
import com.university.home.service.CustomUserDetails;

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

    private final CourseRecommendationService recommendationService;
   
    // [API] AI 강의 추천 기능
    // GET http://localhost:8888/api/course/recommend?studentId=1
    @GetMapping("/recommend")
    public ResponseEntity<Map<String, String>> recommendCourses(@AuthenticationPrincipal CustomUserDetails loginUser
) {
    	Long studentId = loginUser.getUser().getId();
        String recommendation = recommendationService.recommendCourses(studentId);
        // JSON 형태로 반환 { "result": "추천 강의는..." }
        return ResponseEntity.ok(Map.of("result", recommendation));
    }
    
 // GET /api/course/list
    @GetMapping("/list")
    public ResponseEntity<List<Subject>> getCourseList(
            @RequestParam(name = "year", required = false) Long year,
            @RequestParam(name = "semester", required = false) Long semester
    ) {
    
        // [수정] year와 semester가 없으면 null인 상태로 서비스에 넘깁니다.
        // 서비스(CourseService)가 알아서 DB 최신 값을 찾아 쓸 겁니다.
        return ResponseEntity.ok(recommendationService.getAvailableCourses(year, semester));
    }

    // 2. 나의 수강 내역 조회
    // GET /api/course/history?studentId=1
    @GetMapping("/myhistory")
    public ResponseEntity<List<StuSub>> getMyHistory(@AuthenticationPrincipal CustomUserDetails loginUser) {
    	Long studentId = loginUser.getUser().getId();
        return ResponseEntity.ok(recommendationService.getMyCourseHistory(studentId));
    }

    // 3. 수강 신청 실행
    // POST /api/course/register
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Map<String, Long> request) {
        Long studentId = request.get("studentId");
        Long subjectId = request.get("subjectId");
        
        try {
            String msg = recommendationService.registerCourse(studentId, subjectId);
            return ResponseEntity.ok(msg);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
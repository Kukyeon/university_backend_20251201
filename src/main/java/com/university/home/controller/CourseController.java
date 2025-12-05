package com.university.home.controller;

import com.university.home.service.CourseRecommendationService;
import com.university.home.service.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}
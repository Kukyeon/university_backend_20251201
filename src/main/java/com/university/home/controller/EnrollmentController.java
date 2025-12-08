package com.university.home.controller;

import com.university.home.service.CustomUserDetails;
import com.university.home.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/enrollment")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // 수강신청 API
    @PostMapping("/register")
    public ResponseEntity<String> register(
            // [핵심] 토큰을 해석한 결과가 여기(principal)에 자동으로 들어옵니다!
            @AuthenticationPrincipal CustomUserDetails loginUser, 
            @RequestBody Map<String, Long> request) {

        // 토큰이 없거나 만료되면 principal이 null일 수 있음 (필터에서 막겠지만 안전하게)
        if (loginUser == null) {
             return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            // 1. PrincipalDetails 안에 있는 User 객체에서 ID를 꺼냄
            // (주의: User테이블의 ID와 Student테이블의 ID가 같다면 바로 사용)
        	Long studentId = loginUser.getUser().getId();
            
            // 만약 User 테이블과 Student 테이블이 분리되어 있고 User가 Student를 참조한다면:
            // Long studentId = principal.getUser().getStudent().getId(); 

            Long subjectId = request.get("subjectId");

            // 2. 서비스로 전달 (여기서 studentId는 토큰에서 나온 진짜 본인 ID)
            enrollmentService.enroll(studentId, subjectId);
            
            return ResponseEntity.ok("✅ 수강신청 성공!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ 실패: " + e.getMessage());
        }
    }

    // 수강취소 API
    @DeleteMapping("/cancel")
    public ResponseEntity<String> cancel(
            @AuthenticationPrincipal CustomUserDetails loginUser, 
            @RequestParam("subjectId") Long subjectId) {
        
        try {
        	Long studentId = loginUser.getUser().getId();
            
            enrollmentService.cancel(studentId, subjectId);
            return ResponseEntity.ok("🗑️ 수강취소 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ 취소 실패: " + e.getMessage());
        }
    }
}
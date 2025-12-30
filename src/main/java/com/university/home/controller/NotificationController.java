package com.university.home.controller;

import com.university.home.dto.DirectMessageRequest;
import com.university.home.dto.NotificationResponseDto;
import com.university.home.service.CustomUserDetails;
import com.university.home.service.NotificationService; // 서비스 import
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 내 알림목록 조회 ( 교수 -> 학생 알림보내기 수정중 , Dto 변화아안 )
    @GetMapping("/my")
    public ResponseEntity<List<NotificationResponseDto>> getMyNotifications(@AuthenticationPrincipal CustomUserDetails loginUser) {
        if (loginUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(notificationService.getMyNotifications(loginUser.getUser().getId()));
    }

    // 알림 읽음 처리
    @PutMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(@PathVariable("id") Long id) {
        // 서비스 호출
        notificationService.markAsRead(id);
        return ResponseEntity.ok("읽음 처리 완료");
    }
    
    // 알림 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotification(
            @PathVariable("id") Long id, 
            @AuthenticationPrincipal CustomUserDetails loginUser) {
        
        if (loginUser == null) {
            return ResponseEntity.status(401).build();
        }

        notificationService.deleteNotification(id, loginUser.getUser().getId());
        
        return ResponseEntity.ok("알림이 삭제되었습니다.");
    }
    
    // 실시간 알림 구독 (MIME Type: text/event-stream)
    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails loginUser) {
        return notificationService.subscribe(loginUser.getUser().getId());
    }
    
    // 교수 -> 학생 알림보내기
    @PostMapping("/send-direct")
    public ResponseEntity<String> sendDirectMassege(
    		@RequestBody DirectMessageRequest request,
    		@AuthenticationPrincipal CustomUserDetails loginUser) {
    	
    	if (loginUser == null) {
           return ResponseEntity.status(403).body("로그인 정보가 유효하지 않습니다.");
       }

       String userRole = loginUser.getUser().getUserRole();
       if (!userRole.equalsIgnoreCase("PROFESSOR")) {
           return ResponseEntity.status(403).body("교수 권한이 필요합니다.");
       }
       
       try {
           notificationService.sendDirectMessage(
               loginUser.getUser().getId(),
               request.getTargetStudentId(),
               request.getContent()
           );
            return ResponseEntity.ok("메시지 전송 완료");
       } catch (Exception e) {
            return ResponseEntity.status(500).body("알림 전송 중 오류가 발생했습니다.");
       }
    }
    
}
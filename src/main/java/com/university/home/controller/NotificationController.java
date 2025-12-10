package com.university.home.controller;

import com.university.home.entity.Notification;
import com.university.home.service.CustomUserDetails;
import com.university.home.service.NotificationService; // 서비스 import
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService; // 리포지토리 대신 서비스 주입

    // 1. 내 알림 목록 조회
    @GetMapping("/my")
    public ResponseEntity<List<Notification>> getMyNotifications(@AuthenticationPrincipal CustomUserDetails loginUser) {
        if (loginUser == null) {
            return ResponseEntity.status(401).build();
        }
        // 서비스 호출
        return ResponseEntity.ok(notificationService.getMyNotifications(loginUser.getUser().getId()));
    }

    // 2. 알림 읽음 처리
    @PutMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(@PathVariable("id") Long id) {
        // 서비스 호출
        notificationService.markAsRead(id);
        return ResponseEntity.ok("읽음 처리 완료");
    }
    
 // [추가] 알림 삭제 API
    // DELETE /api/notification/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotification(
            @PathVariable("id") Long id, 
            @AuthenticationPrincipal CustomUserDetails loginUser) {
        
        if (loginUser == null) {
            return ResponseEntity.status(401).build();
        }

        // 서비스 호출 (삭제할 알림 ID와 요청한 사람의 ID를 같이 넘김)
        notificationService.deleteNotification(id, loginUser.getUser().getId());
        
        return ResponseEntity.ok("알림이 삭제되었습니다.");
    }
    
 // [추가] 실시간 알림 구독 (MIME Type: text/event-stream)
    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails loginUser) {
        return notificationService.subscribe(loginUser.getUser().getId());
    }
}
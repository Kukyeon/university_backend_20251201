package com.university.home.controller;

import com.university.home.dto.DirectMessageRequest;
import com.university.home.dto.NotificationResponseDto;
import com.university.home.entity.Notification;
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

    private final NotificationService notificationService; // 리포지토리 대신 서비스 주입

    // 1. 내 알림 목록 조회
//    @GetMapping("/my")
//    public ResponseEntity<List<Notification>> getMyNotifications(@AuthenticationPrincipal CustomUserDetails loginUser) {
//        if (loginUser == null) {
//            return ResponseEntity.status(401).build();
//        }
//        // 서비스 호출
//        return ResponseEntity.ok(notificationService.getMyNotifications(loginUser.getUser().getId()));
//    }
    // 내 알림목록 조회 ( 교수 -> 학생 알림보내기 수정중 , Dto 변화아안 )
    // 에러시 해당코드 주석 후 기존꺼 주석해재(Service도 마찬가지)
    @GetMapping("/my")
    public ResponseEntity<List<NotificationResponseDto>> getMyNotifications(@AuthenticationPrincipal CustomUserDetails loginUser) {
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
    
    // 교수 -> 학생 알림보내기@_@
    @PostMapping("/send-direct")
    public ResponseEntity<String> sendDirectMassege(
    		@RequestBody DirectMessageRequest request,
    		@AuthenticationPrincipal CustomUserDetails loginUser) {
    	
    	if (loginUser == null) {
            log.warn("🚨 [403 ERROR] 로그인 사용자 객체가 Null입니다. (토큰 문제 가능성)");
           return ResponseEntity.status(403).body("로그인 정보가 유효하지 않습니다.");
       }

       String userRole = loginUser.getUser().getUserRole();
       log.info("🔔 알림 전송 시도 사용자: ID={}, Role={}", loginUser.getUser().getId(), userRole);
       if (!userRole.equalsIgnoreCase("PROFESSOR")) {
           log.warn("🚨 [403 ERROR] 필요한 권한(PROFESSOR)과 현재 권한({})이 일치하지 않습니다.", userRole);
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
            log.error("메시지 전송 중 서비스 오류 발생", e);
            return ResponseEntity.status(500).body("알림 전송 중 오류가 발생했습니다.");
       }
    }
    
}
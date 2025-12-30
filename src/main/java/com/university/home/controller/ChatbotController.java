package com.university.home.controller;

import com.university.home.entity.ChatLog;
import com.university.home.service.ChatbotService;
import com.university.home.service.CustomUserDetails;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    // 챗봇에게 질문을 보내고 답변을 받는 API
    @PostMapping("/ask")
    public ResponseEntity<ChatBotResponse> ask(@RequestBody ChatBotRequest request) {

        // 서비스 계층 호출
        String answer = chatbotService.ask(request.getStudentId(), request.getQuestion());

        return ResponseEntity.ok(new ChatBotResponse(answer));
    }
    // 채팅 내역
    @GetMapping("/history")
    public ResponseEntity<List<ChatLog>> getChatHistory(@AuthenticationPrincipal CustomUserDetails loginUser) {
    	Long studentId = loginUser.getUser().getId();
        List<ChatLog> history = chatbotService.getChatHistory(studentId);
        return ResponseEntity.ok(history);
    }
    // 채팅 내역 삭제
    @DeleteMapping("/history")
    public ResponseEntity<String> claerHistory(@AuthenticationPrincipal CustomUserDetails loginUser) {
    	Long studentId = loginUser.getUser().getId();
    	chatbotService.clearChatHistory(studentId);
    	return ResponseEntity.ok("대화가 종료되었습니다");
    }
    
    // --- DTO 클래스 ---
    @Data
    public static class ChatBotRequest {
        private Long studentId;  // 학생 ID
        private String question; // 실제 질문 내용
    }

    @Data
    public static class ChatBotResponse {
        private final String answer; // 답변
    }
}
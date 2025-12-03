package com.university.home.controller;

import com.university.home.entity.ChatLog;
import com.university.home.service.ChatbotService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    /**
     * 챗봇에게 질문을 보내고 답변을 받는 API
     * URL: POST /api/chatbot/ask
     */
    @PostMapping("/ask")
    public ResponseEntity<ChatBotResponse> ask(@RequestBody ChatBotRequest request) {
        log.info("챗봇 요청 - studentId: {}, question: {}", request.getStudentId(), request.getQuestion());

        // 서비스 계층 호출
        String answer = chatbotService.ask(request.getStudentId(), request.getQuestion());

        log.info("챗봇 답변: {}", answer);
        return ResponseEntity.ok(new ChatBotResponse(answer));
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<ChatLog>> getChatHistory(@RequestParam("studentId") Long studentId) {
        // ChatLogRepository에 findByStudentIdOrderByCreatedAtAsc 메서드가 있어야 합니다.
        // (과거 대화부터 순서대로 보여줘야 하므로 Asc 오름차순 사용) //프론트 확인후 수정
    	// [수정] 리포지토리가 아니라 서비스를 호출합니다.
        List<ChatLog> history = chatbotService.getChatHistory(studentId);
        return ResponseEntity.ok(history);
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
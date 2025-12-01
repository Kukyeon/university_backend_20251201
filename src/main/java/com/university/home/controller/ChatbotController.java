package com.university.home.controller;

import com.university.home.service.ChatbotService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
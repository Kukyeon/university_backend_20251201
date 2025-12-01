package com.university.home.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.university.home.service.GeminiService;

@RestController // 이게 없으면 404 뜸
@RequiredArgsConstructor
public class TestController {

    private final GeminiService geminiService;

    // http://localhost:8888/test/gemini?message=안녕
    @GetMapping("/test/gemini") // 주소 오타 확인
    public String testGemini(@RequestParam("message") String message) {
        System.out.println("질문 도착: " + message); // 콘솔에 로그 찍히는지 확인용
        return geminiService.talk(message);
    }
}
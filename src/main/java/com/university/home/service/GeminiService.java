package com.university.home.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.university.home.dto.GeminiRequestDto;
import com.university.home.dto.GeminiResponseDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    
    private static final String MODEL = "gemini-2.5-flash";
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent";

    private final RestTemplate restTemplate = new RestTemplate();

    public String talk(String message) {

        // 요청 JSON 구조 생성
        GeminiRequestDto request = GeminiRequestDto.of(message);

        String requestUrl = API_URL + "?key=" + apiKey;

        try {
            GeminiResponseDto response =
                    restTemplate.postForObject(requestUrl, request, GeminiResponseDto.class);

            if (response != null &&
                response.getCandidates() != null &&
                !response.getCandidates().isEmpty() &&
                response.getCandidates().get(0).getContent() != null &&
                !response.getCandidates().get(0).getContent().getParts().isEmpty()) {

                return response.getCandidates().get(0).getContent().getParts().get(0).getText();
            }

            return "AI가 응답하지 않습니다.";

        } catch (Exception e) {
            return "AI 서버 연결 실패: " + e.getMessage();
        }
    }
}

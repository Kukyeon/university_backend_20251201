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

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String talk(String message) {
        // 1. 요청 주소 만들기 (URL + Key)
        String requestUrl = apiUrl + "?key=" + apiKey;

        // 2. 데이터 포장하기
        GeminiRequestDto request = new GeminiRequestDto(message);

        try {
            // 3. 구글 서버로 발송! (POST 요청)
            GeminiResponseDto response = restTemplate.postForObject(requestUrl, request, GeminiResponseDto.class);

            // 4. 답변 꺼내기 (복잡한 JSON에서 text만 쏙 뺌)
            if (response != null && !response.getCandidates().isEmpty()) {
                return response.getCandidates().get(0).getContent().getParts().get(0).getText();
            }
            return "답변을 들을 수 없습니다.";
            
        } catch (Exception e) {
            log.error("Gemini 연결 에러: ", e);
            return "AI 서버 연결 실패: " + e.getMessage();
        }
    }
}
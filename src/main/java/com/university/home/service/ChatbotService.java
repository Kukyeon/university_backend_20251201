package com.university.home.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // jakarta 대신 springframework 권장

import com.university.home.entity.ChatLog;
import com.university.home.entity.Student;
import com.university.home.repository.ChatLogRepository;
import com.university.home.repository.StudentRepository; // [필수] 학생을 찾기 위해 추가

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final GeminiService geminiService;
    private final ChatLogRepository chatLogRepository;
    private final StudentRepository studentRepository; // [추가] 리포지토리 주입

    @Transactional
    public String ask(Long studentId, String question) {
        
        // 1. 넘어온 ID(숫자)로 DB에서 실제 학생 객체를 가져옵니다.
        // (만약 없는 ID라면 에러를 냅니다)
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다. ID: " + studentId));

        // 2. 시스템 프롬프트 구성 (AI 설정)
        String systemPrompt = """
                당신은 '그린대학교'의 친절한 학사 행정 챗봇입니다.
                [학사 규정 요약]
                - 수강신청 기간: 2월 10일 ~ 2월 14일
                - 졸업 요건: 총 130학점 이수
                [질문]: %s
                """.formatted(question);

        // 3. Gemini에게 질문 및 답변 받기
        String answer = geminiService.talk(systemPrompt);

        // 4. 대화 내용 저장
        // 여기서 아까 찾아둔 'student' 객체를 쏙 넣어줍니다.
        ChatLog log = ChatLog.builder()
                .student(student)        // [핵심] 숫자가 아니라 객체를 넣음
                .question(question)
                .answer(answer)
                .createdAt(LocalDateTime.now())
                .build();
        
        chatLogRepository.save(log);

        return answer;
    }
}
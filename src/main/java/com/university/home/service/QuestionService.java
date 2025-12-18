package com.university.home.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.university.home.dto.QuestionDto;
import com.university.home.entity.Question;
import com.university.home.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    @Transactional(readOnly = true)
    public QuestionDto getQuestions() {
        // DB 스키마를 따르기 위해, 모든 질문 레코드를 가져와 그 중 첫 번째(유일한) 레코드를 사용합니다.
        List<Question> questionsList = questionRepository.findAll();
        QuestionDto dto = new QuestionDto();

        if (!questionsList.isEmpty()) {
            Question questionRecord = questionsList.get(0); // 첫 번째 레코드 사용

           
            
            // 만약 Question 엔티티에 question1~question7 필드가 있다면:
            dto.setQuestion1(questionRecord.getQuestion1());
            dto.setQuestion2(questionRecord.getQuestion2());
            dto.setQuestion3(questionRecord.getQuestion3());
            dto.setQuestion4(questionRecord.getQuestion4());
            dto.setQuestion5(questionRecord.getQuestion5());
            dto.setQuestion6(questionRecord.getQuestion6());
            dto.setQuestion7(questionRecord.getQuestion7());

            // 이전 코드에서 사용하던 content 필드는 더 이상 사용하지 않습니다.
            // dto.setSugContent(questionRecord.getSugContent()); // 필요 시 추가
        } else {
             // 데이터베이스에 질문 레코드가 없는 경우 로그 처리 또는 기본값 설정
             System.out.println("ERROR: question_tb에 질문 레코드가 존재하지 않습니다.");
        }

        return dto;
    }
}

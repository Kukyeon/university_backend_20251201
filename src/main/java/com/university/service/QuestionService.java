package com.university.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.university.dto.QuestionDto;
import com.university.entity.Question;
import com.university.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    @Transactional(readOnly = true)
    public QuestionDto getQuestions() {
        List<Question> questions = questionRepository.findAll();
        QuestionDto dto = new QuestionDto();

        if (questions.size() >= 7) {
            dto.setQuestion1(questions.get(0).getContent());
            dto.setQuestion2(questions.get(1).getContent());
            dto.setQuestion3(questions.get(2).getContent());
            dto.setQuestion4(questions.get(3).getContent());
            dto.setQuestion5(questions.get(4).getContent());
            dto.setQuestion6(questions.get(5).getContent());
            dto.setQuestion7(questions.get(6).getContent());
        }

        dto.setSugContent(null); // 필요 시 세팅
        return dto;
    }
}

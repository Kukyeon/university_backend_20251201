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
        List<Question> questionsList = questionRepository.findAll();
        QuestionDto dto = new QuestionDto();

        if (!questionsList.isEmpty()) {
            Question questionRecord = questionsList.get(0);
            
            dto.setQuestion1(questionRecord.getQuestion1());
            dto.setQuestion2(questionRecord.getQuestion2());
            dto.setQuestion3(questionRecord.getQuestion3());
            dto.setQuestion4(questionRecord.getQuestion4());
            dto.setQuestion5(questionRecord.getQuestion5());
            dto.setQuestion6(questionRecord.getQuestion6());
            dto.setQuestion7(questionRecord.getQuestion7());

        }
        return dto;
    }
}

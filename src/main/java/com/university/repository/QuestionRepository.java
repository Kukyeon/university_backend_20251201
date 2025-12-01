package com.university.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    // 모든 질문 조회
}

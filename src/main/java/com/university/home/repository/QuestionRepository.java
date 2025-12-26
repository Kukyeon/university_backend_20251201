package com.university.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}

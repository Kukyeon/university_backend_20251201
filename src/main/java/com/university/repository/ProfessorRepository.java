package com.university.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.entity.Professor;

public interface ProfessorRepository extends JpaRepository< Professor, Long> {

}

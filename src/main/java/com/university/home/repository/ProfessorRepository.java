package com.university.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.Professor;

public interface ProfessorRepository extends JpaRepository< Professor, Long> {

}

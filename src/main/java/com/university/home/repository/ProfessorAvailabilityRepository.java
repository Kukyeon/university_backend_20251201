package com.university.home.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.ProfessorAvailability;

public interface ProfessorAvailabilityRepository extends JpaRepository<ProfessorAvailability, Long> {
    List<ProfessorAvailability> findByProfessorIdAndIsBooked(Long professorId, boolean isBooked);
}

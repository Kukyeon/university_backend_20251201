package com.university.home.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.ProfessorAvailability;

public interface ProfessorAvailabilityRepository extends JpaRepository<ProfessorAvailability, Long> {
  
	//  특정 교수의 예약 가능 시간 조회 (교수 페이지용)
	List<ProfessorAvailability> findByProfessorIdAndIsBooked(Long professorId, boolean isBooked);
    
	//  모든 교수의 예약 가능한 시간 조회 (학생 예약 페이지용)
    List<ProfessorAvailability> findByIsBooked(boolean isBooked);
}

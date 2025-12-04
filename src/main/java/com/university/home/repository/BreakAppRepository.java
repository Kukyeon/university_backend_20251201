package com.university.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.BreakApp;
import com.university.home.entity.Student;

import java.util.List;


public interface BreakAppRepository extends JpaRepository<BreakApp, Long> {

	List<BreakApp> findByStudentOrderByIdDesc(Student student);
	
	List<BreakApp> findByStatus(String status);
	
	List<BreakApp> findByStudentId(Long studentId);
}

package com.university.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.StuStat;
import java.util.List;
import java.util.Optional;


public interface StuStatRepository extends JpaRepository<StuStat, Long>{

	List<StuStat> findByStudentIdOrderByIdDesc(Long studentId);
	
	
}

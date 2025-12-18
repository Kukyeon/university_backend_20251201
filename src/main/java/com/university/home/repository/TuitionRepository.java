package com.university.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.Tuition;
import java.util.List;
import java.util.Optional;


public interface TuitionRepository extends JpaRepository<Tuition,Long>{

	List<Tuition> findByStudentId(Long studentId);
	
	List<Tuition> findByStudentIdAndStatus(Long studentId, Boolean status);
	
	Optional<Tuition> findByStudentIdAndTuiYearAndSemester(Long studentId, Long tuiYear, Long semester);

}

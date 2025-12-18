package com.university.home.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.StuSch;
import com.university.home.entity.Student;

public interface StuSchRepository extends JpaRepository<StuSch, Long> {

	List<StuSch> findByStudent(Student student);
	
	List<StuSch> findByStudentIdAndSchYearAndSemester(Long studentId, Long schYear, Long semester);
}

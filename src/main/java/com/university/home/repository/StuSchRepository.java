package com.university.home.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.StuSch;

public interface StuSchRepository extends JpaRepository<StuSch, Long> {

	List<StuSch> findByStudentIdAndSchYearAndSemester(Long studentId, Long schYear, Long semeste);
}

package com.university.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.CollTuit;
import java.util.List;


public interface CollTuitRepository extends JpaRepository<CollTuit, Long>{

	boolean existsByCollege_Id(Long collegeId);
	
	//findByCollegeIdAnd(Long collegeId);
}

package com.university.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.CollTuit;
import java.util.List;
import java.util.Optional;

import com.university.home.entity.College;



public interface CollTuitRepository extends JpaRepository<CollTuit, Long>{

	boolean existsByCollege_Id(Long collegeId);
	
	Optional<CollTuit> findByCollege(College college);
}

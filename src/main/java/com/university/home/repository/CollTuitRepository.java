package com.university.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.CollTuit;

public interface CollTuitRepository extends JpaRepository<CollTuit, Long>{

	boolean existsByCollegeId(Long collegeId);
}

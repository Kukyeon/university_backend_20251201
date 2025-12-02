package com.university.home.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.Scholarship;
import java.util.List;


public interface ScholarshipRepository extends JpaRepository<Scholarship, Long> {

	Optional<Scholarship> findByType(Long type);
}

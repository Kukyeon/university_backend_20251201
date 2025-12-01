package com.university.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.entity.Scholarship;

public interface ScholarshipRepository extends JpaRepository<Scholarship, Long> {

}

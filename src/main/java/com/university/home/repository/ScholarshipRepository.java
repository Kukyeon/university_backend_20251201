package com.university.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.Scholarship;

public interface ScholarshipRepository extends JpaRepository<Scholarship, Long> {

}

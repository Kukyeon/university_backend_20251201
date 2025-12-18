package com.university.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

}

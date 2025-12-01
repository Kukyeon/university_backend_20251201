package com.university.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.entity.Student;
import java.util.List;


public interface StudentRepository extends JpaRepository<Student, Long> {

	// 이름 + 이메일(아이디 찾기)
	Optional<Student> findByNameAndEmail(String name, String email);
	
	// 이름 + 이메일 + ID 존재 여부
	boolean existsByIdAndNameAndEmail(Long id, String name, String email);
}

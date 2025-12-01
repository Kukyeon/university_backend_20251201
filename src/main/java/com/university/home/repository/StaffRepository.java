package com.university.home.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.Staff;

import java.util.List;


public interface StaffRepository extends JpaRepository<Staff, Long> {

	// 이름 + 이메일(아이디 찾기)
	Optional<Staff> findByNameAndEmail(String name, String email);

	// 이름 + 이메일 + ID 존재 여부
	boolean existsByIdAndNameAndEmail(Long id, String name, String email);
}

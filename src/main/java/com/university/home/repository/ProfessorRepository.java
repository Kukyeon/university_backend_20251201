package com.university.home.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.Professor;

public interface ProfessorRepository extends JpaRepository< Professor, Long> {

	// 이름 + 이메일(아이디 찾기)
		Optional<Professor> findByNameAndEmail(String name, String email);
		
		// 이름 + 이메일 + ID 존재 여부
		boolean existsByIdAndNameAndEmail(Long id, String name, String email);

		 // 전체 학생 페이지 조회
	    Page<Professor> findAll(Pageable pageable);

	    // 학과별 학생 페이지 조회
	    Page<Professor> findByDepartmentId(Long deptId, Pageable pageable);

	    // 학번으로 학생 조회
	    Page<Professor> findByProfessorId(Long professorId, Pageable pageable);
}

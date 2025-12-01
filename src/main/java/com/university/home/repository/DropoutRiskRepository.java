package com.university.home.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.DropoutRisk;

public interface DropoutRiskRepository extends JpaRepository<DropoutRisk, Long>{
	// 특정 학생의 위험 분석 이력 조회
    List<DropoutRisk> findByStudentIdOrderByAnalyzedDateDesc(Long studentId);
    
    // [관리자용] 위험도가 높은 학생들만 조회 (예: '위험' 등급)
    List<DropoutRisk> findByRiskLevel(String riskLevel);
	
}

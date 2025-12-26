package com.university.home.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.DropoutRisk;

public interface DropoutRiskRepository extends JpaRepository<DropoutRisk, Long>{
	// 특정 학과의 위험군 학생 목록 조회
    List<DropoutRisk> findByStudentDepartmentIdOrderByRiskScoreDesc(Long deptId);

    // [관리자] 전체 학과의 '심각' 등급 학생만 조회
    List<DropoutRisk> findByRiskLevelOrderByRiskScoreDesc(String riskLevel);
    
    // [통계] 특정 학과의 위험(Level) 학생 수 카운트
    long countByStudentDepartmentIdAndRiskLevel(Long deptId, String riskLevel);
	
    Optional<DropoutRisk> findTopByStudentIdOrderByAnalyzedDateDesc(Long studentId);
    // 전교생 조회 (위험 점수 높은 순서대로)
    List<DropoutRisk> findAllByOrderByRiskScoreDesc(); 
    
}

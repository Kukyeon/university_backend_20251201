package com.university.home.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.DropoutRisk;

public interface DropoutRiskRepository extends JpaRepository<DropoutRisk, Long>{
	// [핵심 기능] 특정 학과의 위험군 학생 목록 조회
    // 해석: DropoutRisk 안의 student 안의 department 안의 id가 일치하는 것 찾기
    List<DropoutRisk> findByStudentDepartmentIdOrderByRiskScoreDesc(Long deptId);

    // [관리자용] 전체 학과의 '위험' 등급 학생만 조회
    List<DropoutRisk> findByRiskLevelOrderByRiskScoreDesc(String riskLevel);
    
    // [통계용] 특정 학과의 위험(Level) 학생 수 카운트
    long countByStudentDepartmentIdAndRiskLevel(Long deptId, String riskLevel);
	
}

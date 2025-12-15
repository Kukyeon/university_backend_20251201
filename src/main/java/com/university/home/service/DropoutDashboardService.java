package com.university.home.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.university.home.dto.DropoutRiskResponseDto;
import com.university.home.entity.DropoutRisk;
import com.university.home.entity.Professor;
import com.university.home.repository.DropoutRiskRepository;
import com.university.home.repository.ProfessorRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DropoutDashboardService {

    private final DropoutRiskRepository dropoutRiskRepository;
    private final ProfessorRepository professorRepository;
    
    // [교수님용] 소속 학과 학생 중 '심각' 단계만 조회
    @Transactional
    public List<DropoutRiskResponseDto> getProfessorDashboard(Long professorId){
        // 1. 교수님 정보 조회
        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new IllegalArgumentException("교수님 정보를 찾을 수 없습니다."));
        
        // 2. 교수님 소속 학과 ID 추출
        Long deptId = professor.getDepartment().getId();

        // 3. 해당 학과의 모든 위험 데이터 조회 (일단 다 가져오고나서 필터링)
        // (기존 메서드 재사용하거나, findAllByStudentDepartmentId 등 사용)
        List<DropoutRisk> risks = dropoutRiskRepository.findByStudentDepartmentIdOrderByRiskScoreDesc(deptId);

        // 4. 필터링 및 정렬 적용
        return risks.stream()
                // [필터] '심각' 단계만 남기기
                .filter(risk -> "심각".equals(risk.getRiskLevel()))
                // [정렬] 1순위: 날짜(최신순), 2순위: 이름(가나다순)
                .sorted(Comparator.comparing(DropoutRisk::getAnalyzedDate).reversed()
                        .thenComparing(risk -> risk.getStudent().getName()))
                // [변환] DTO로 변환
                .map(DropoutRiskResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // [관리자용] 전교생 중 '심각' 단계만 조회
    @Transactional
    public List<DropoutRiskResponseDto> getAllRiskStudents() {
        // 1. 전체 데이터 가져오기
        List<DropoutRisk> allRisks = dropoutRiskRepository.findAllByOrderByRiskScoreDesc();

        // 2. 필터링 및 정렬 적용
        return allRisks.stream()
                // [필터] '심각' 단계만
                .filter(risk -> "심각".equals(risk.getRiskLevel()))
                // [정렬] 1순위: 날짜(최신순), 2순위: 이름(가나다순)
                .sorted(Comparator.comparing(DropoutRisk::getAnalyzedDate).reversed()
                        .thenComparing(risk -> risk.getStudent().getName()))
                .map(DropoutRiskResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
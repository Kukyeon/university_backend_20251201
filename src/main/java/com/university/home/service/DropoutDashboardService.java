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
    
    // [교수용] 소속 학과 학생 중 '심각' 단계만 조회
    @Transactional
    public List<DropoutRiskResponseDto> getProfessorDashboard(Long professorId){
        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new IllegalArgumentException("교수님 정보를 찾을 수 없습니다."));
        
        Long deptId = professor.getDepartment().getId();

        List<DropoutRisk> risks = dropoutRiskRepository.findByStudentDepartmentIdOrderByRiskScoreDesc(deptId);

        return risks.stream()
                .filter(risk -> "심각".equals(risk.getRiskLevel()))
                .sorted(Comparator.comparing(DropoutRisk::getAnalyzedDate).reversed()
                        .thenComparing(risk -> risk.getStudent().getName()))
                .map(DropoutRiskResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // [관리자용] 전교생 중 '심각' 단계만 조회
    @Transactional
    public List<DropoutRiskResponseDto> getAllRiskStudents() {
        List<DropoutRisk> allRisks = dropoutRiskRepository.findAllByOrderByRiskScoreDesc();

        return allRisks.stream()
                .filter(risk -> "심각".equals(risk.getRiskLevel()))
                .sorted(Comparator.comparing(DropoutRisk::getAnalyzedDate).reversed()
                        .thenComparing(risk -> risk.getStudent().getName()))
                .map(DropoutRiskResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
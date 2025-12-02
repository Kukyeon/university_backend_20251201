package com.university.home.service;

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
	
	@Transactional
    public List<DropoutRiskResponseDto> getProfessorDashboard(Long professorId){
		// 1. 교수님 정보 조회 (소속 학과를 알기 위해)
        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new IllegalArgumentException("교수님 정보를 찾을 수 없습니다."));
        
        // 2. 교수님 소속 학과 ID 추출
        Long deptId = professor.getDepartment().getId();

        // 3. 해당 학과의 위험 분석 데이터 조회 (위험점수 높은 순)
        List<DropoutRisk> risks = dropoutRiskRepository.findByStudentDepartmentIdOrderByRiskScoreDesc(deptId);

        // 4. Entity 리스트를 DTO 리스트로 변환해서 반환
        return risks.stream()
                .map(DropoutRiskResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}

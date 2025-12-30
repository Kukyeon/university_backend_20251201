package com.university.home.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.university.home.dto.EvaluationDto;
import com.university.home.entity.Evaluation;
import com.university.home.entity.StuSub;
import com.university.home.repository.EvaluationRepository;
import com.university.home.repository.StuSubRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final StuSubRepository stuSubRepository;

    @Transactional
    public Evaluation createEvaluation(EvaluationDto dto) {
    	StuSub stuSub = stuSubRepository
    		    .findByStudentIdAndSubjectId(dto.getStudentId(), dto.getSubjectId())
    		    .orElseThrow(() -> new RuntimeException("수강 정보가 없습니다."));

        Evaluation evaluation = new Evaluation();
        evaluation.setStuSub(stuSub);
        evaluation.setAnswer1(dto.getAnswer1());
        evaluation.setAnswer2(dto.getAnswer2());
        evaluation.setAnswer3(dto.getAnswer3());
        evaluation.setAnswer4(dto.getAnswer4());
        evaluation.setAnswer5(dto.getAnswer5());
        evaluation.setAnswer6(dto.getAnswer6());
        evaluation.setAnswer7(dto.getAnswer7());
        evaluation.setImprovements(dto.getImprovements());
        
        return evaluationRepository.save(evaluation);
    }
    private double calculateAvg(Evaluation e) {
        double sum = 
            e.getAnswer1() + e.getAnswer2() + e.getAnswer3() +
            e.getAnswer4() + e.getAnswer5() + e.getAnswer6() +
            e.getAnswer7();

        return sum / 7.0;
    }

    //  단일 평가 ID로 상세 정보 조회
    public Evaluation getEvaluationById(Long id) {
        return evaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("평가 ID: " + id + "에 해당하는 평가를 찾을 수 없습니다."));
    }

    public List<Evaluation> getEvaluationByStudentId(Long studentId) {
        return evaluationRepository.findByStuSub_Student_Id(studentId);
    }

    // 교수 기준 전체 평가 → DTO로 변환
    public Page<EvaluationDto> getEvaluationsByProfessorId(Long professorId, Pageable pageable) {
        return evaluationRepository.findByProfessorId(professorId, pageable)
                .map(this::convertToDto);
    }
    public List<String> getSubjectsByProfessor(Long professorId) {
        return evaluationRepository.findDistinctSubjectNameByProfessorId(professorId);
    }

    public List<EvaluationDto> getEvaluationsByProfessorAndSubject(Long professorId, String subjectName) {
        return evaluationRepository.findByProfessorIdAndSubjectName(professorId, subjectName).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Evaluation → MyEvaluationDto 변환
    private EvaluationDto convertToDto(Evaluation e) {
        EvaluationDto dto = new EvaluationDto();
        dto.setStudentId(e.getStuSub().getStudent().getId());
        dto.setStudentName(e.getStuSub().getStudent().getName());
        dto.setSubjectId(e.getStuSub().getSubject().getId());
        dto.setSubjectName(e.getStuSub().getSubject().getName());
        dto.setProfessorId(e.getStuSub().getSubject().getProfessor().getId());
        dto.setAnswer1(e.getAnswer1());
        dto.setAnswer2(e.getAnswer2());
        dto.setAnswer3(e.getAnswer3());
        dto.setAnswer4(e.getAnswer4());
        dto.setAnswer5(e.getAnswer5());
        dto.setAnswer6(e.getAnswer6());
        dto.setAnswer7(e.getAnswer7());
        dto.setImprovements(e.getImprovements());
        
        dto.setAvgScore(calculateAvg(e));
        return dto;
    }
}
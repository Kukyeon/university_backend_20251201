package com.university.home.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.university.home.dto.EvaluationDto;
import com.university.home.dto.MyEvaluationDto;
import com.university.home.entity.Evaluation;
import com.university.home.entity.Student;
import com.university.home.entity.Subject;
import com.university.home.repository.EvaluationRepository;
import com.university.home.repository.StudentRepository;
import com.university.home.repository.SubjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;

    @Transactional
    public Evaluation createEvaluation(EvaluationDto dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("학생이 없습니다."));
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new RuntimeException("과목이 없습니다."));

        Evaluation evaluation = new Evaluation();
        evaluation.setStudent(student);
        evaluation.setSubject(subject);
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

    public Evaluation getEvaluationByStudentId(Long studentId) {
        return evaluationRepository.findByStudent_Id(studentId);
    }

    // 교수 기준 전체 평가 → DTO로 변환
    public List<MyEvaluationDto> getEvaluationsByProfessorId(Long professorId) {
        List<Evaluation> evaluations = evaluationRepository.findBySubject_Professor_Id(professorId);

        return evaluations.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    // 교수+과목 기준 평가 → DTO로 변환
    public List<MyEvaluationDto> getEvaluationsByProfessorAndSubject(Long professorId, String subjectName) {
        List<Evaluation> evaluations = evaluationRepository.findByProfessorIdAndSubjectName(professorId, subjectName);

        return evaluations.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    // Evaluation → MyEvaluationDto 변환
    private MyEvaluationDto convertToDto(Evaluation e) {
        MyEvaluationDto dto = new MyEvaluationDto();
        dto.setProfessorId(e.getSubject().getProfessor().getId());
        dto.setName(e.getSubject().getName());
        dto.setAnswer1(e.getAnswer1());
        dto.setAnswer2(e.getAnswer2());
        dto.setAnswer3(e.getAnswer3());
        dto.setAnswer4(e.getAnswer4());
        dto.setAnswer5(e.getAnswer5());
        dto.setAnswer6(e.getAnswer6());
        dto.setAnswer7(e.getAnswer7());
        dto.setImprovements(e.getImprovements());
        return dto;
    }
}

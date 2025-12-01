package com.university.home.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.university.home.dto.EvaluationDto;
import com.university.home.entity.Evaluation;
import com.university.home.entity.Student;
import com.university.home.entity.Subject;
import com.university.home.repository.EvaluationRepository;
import com.university.home.repository.StudentRepository;
//import com.university.home.repository.SubjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final StudentRepository studentRepository;
   // private final SubjectRepository subjectRepository;

    @Transactional
    public Evaluation createEvaluation(EvaluationDto dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("학생이 없습니다."));
//        Subject subject = subjectRepository.findById(dto.getSubjectId())
//                .orElseThrow(() -> new RuntimeException("과목이 없습니다."));

        Evaluation evaluation = new Evaluation();
        evaluation.setStudent(student);
        //evaluation.setSubject(subject);
        evaluation.setAnswer1((long) dto.getAnswer1());
        evaluation.setAnswer2((long) dto.getAnswer2());
        evaluation.setAnswer3((long) dto.getAnswer3());
        evaluation.setAnswer4((long) dto.getAnswer4());
        evaluation.setAnswer5((long) dto.getAnswer5());
        evaluation.setAnswer6((long) dto.getAnswer6());
        evaluation.setAnswer7((long) dto.getAnswer7());
        evaluation.setImprovements(dto.getImprovements());

        return evaluationRepository.save(evaluation);
    }

    public Evaluation getEvaluationByStudentId(Long studentId) {
        return evaluationRepository.findByStudent_Id(studentId);
    }

    public List<Evaluation> getEvaluationsByProfessorId(Long professorId) {
        return evaluationRepository.findBySubject_Professor_Id(professorId);
    }

    public List<Evaluation> getEvaluationsByProfessorAndSubject(Long professorId, String subjectName) {
        return evaluationRepository.findByProfessorIdAndSubjectName(professorId, subjectName);
    }
}

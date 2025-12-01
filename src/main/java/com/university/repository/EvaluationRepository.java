package com.university.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.university.entity.Evaluation;

import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    // 학생 기준 평가 조회
    Evaluation findByStudent_Id(Long studentId);

    // 교수 기준 전체 강의 평가 조회
    List<Evaluation> findBySubject_Professor_Id(Long professorId);

    // 교수 기준 과목별 강의 평가 조회111
    @Query("SELECT e FROM Evaluation e WHERE e.subject.professor.id = :professorId AND e.subject.name = :subjectName")
    List<Evaluation> findByProfessorIdAndSubjectName(@Param("professorId") Long professorId,
                                                     @Param("subjectName") String subjectName);
}

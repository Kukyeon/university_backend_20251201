package com.university.home.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.university.home.entity.Evaluation;

import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    // 학생 기준 평가 조회
	List<Evaluation> findByStuSub_Student_Id(Long studentId);
	boolean existsByStuSub_Id(Long stuSubId);
	// 교수 기준 조회
    @Query("SELECT e FROM Evaluation e " +
           "WHERE e.stuSub.subject.professor.id = :professorId")
    Page<Evaluation> findByProfessorId(@Param("professorId") Long professorId, Pageable pageable);

    // 교수+과목 기준 조회
    @Query("SELECT e FROM Evaluation e " +
           "WHERE e.stuSub.subject.professor.id = :professorId " +
           "AND e.stuSub.subject.name = :subjectName")
    List<Evaluation> findByProfessorIdAndSubjectName(@Param("professorId") Long professorId,
                                                     @Param("subjectName") String subjectName);
    @Query("SELECT DISTINCT e.stuSub.subject.name " +
            "FROM Evaluation e " + 
            "WHERE e.stuSub.subject.professor.id = :professorId")
    List<String> findDistinctSubjectNameByProfessorId(@Param("professorId")Long professorId);

}

package com.university.home.repository;

import com.university.home.entity.StuSub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StuSubRepository extends JpaRepository<StuSub, Long> {
    
    //학생 ID로 수강 내역 조회
    List<StuSub> findByStudentId(Long studentId);

    //중복 신청 확인
    boolean existsByStudent_IdAndSubject_Id(Long studentId, Long subjectId);
    
    //수강 취소용 조회
    Optional<StuSub> findByStudentIdAndSubjectId(Long studentId, Long subjectId);
    
    //이번 학기 수강 신청 내역 리스트 조회
    List<StuSub> findByStudentIdAndSubjectSubYearAndSubjectSemester(Long studentId, Long subYear, Long semester);

    List<StuSub> findBySubject_Id(Long subjectId);
    
    //특정타입 조회용 (전공/교양)
    List<StuSub> findByStudentIdAndSubjectSubYearAndSubjectSemesterAndSubjectType(
    	    Long studentId, Long year, Long semester, String type
    	);
    @Query("SELECT DISTINCT s.subject.subYear FROM StuSub s " +
            "WHERE s.student.id = :studentId " +
            "ORDER BY s.subject.subYear DESC")
     List<Long> findDistinctYearsByStudentId(@Param("studentId") Long studentId);
}
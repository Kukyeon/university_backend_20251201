package com.university.home.repository;

import com.university.home.entity.StuSub;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StuSubRepository extends JpaRepository<StuSub, Long> {
    
    //학생 ID로 수강 내역(성적 포함)을 몽땅 가져옵니다.
    List<StuSub> findByStudentId(Long studentId);

    //중복 신청 확인
    boolean existsByStudent_IdAndSubject_Id(Long studentId, Long subjectId);
    
    //수강 취소용 조회
    Optional<StuSub> findByStudentIdAndSubjectId(Long studentId, Long subjectId);
    
    //이번 학기 수강 신청 내역 리스트 조회
    List<StuSub> findByStudentIdAndSubjectSubYearAndSubjectSemester(Long studentId, Long subYear, Long semester);
}
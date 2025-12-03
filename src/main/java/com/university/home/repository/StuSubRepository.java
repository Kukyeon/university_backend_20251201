package com.university.home.repository;

import com.university.home.entity.StuSub;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StuSubRepository extends JpaRepository<StuSub, Long> {
    
    // [핵심] 쿼리문 없이 학생 ID로 수강 내역(성적 포함)을 몽땅 가져옵니다.
    List<StuSub> findByStudentId(Long studentId);
}
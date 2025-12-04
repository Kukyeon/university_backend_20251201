package com.university.home.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.StuSubDetail;

public interface StuSubDetailRepository extends JpaRepository<StuSubDetail, Long> {
    
    // [수정] 쿼리문 삭제! -> 단순히 리스트를 가져오는 메서드로 변경
    // 
    List<StuSubDetail> findByStudentId(Long studentId);
}

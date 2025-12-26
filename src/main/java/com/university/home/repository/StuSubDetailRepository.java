package com.university.home.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.StuSubDetail;

public interface StuSubDetailRepository extends JpaRepository<StuSubDetail, Long> {
    
    List<StuSubDetail> findByStudent_Id(Long studentId);
    
    List<StuSubDetail> findBySubject_Id(Long subjectId);
}

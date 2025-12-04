package com.university.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.university.home.entity.DataCollectionLog;
import java.util.List;

public interface DataCollectionLogRepository extends JpaRepository<DataCollectionLog, Long> {

    // 1. 기본 저장(save), 조회(findAll) 등은 JpaRepository가 공짜로 줍니다.

    // [추가 기능] 특정 시스템(예: "LMS")의 로그만 최신순으로 보고 싶을 때 사용
    // 쿼리문 없이 이름 규칙만으로 작동합니다.
    List<DataCollectionLog> findBySourceSystemOrderByCollectedAtDesc(String sourceSystem);
    
    // [추가 기능] 실패한(FAIL) 로그만 보고 싶을 때
    List<DataCollectionLog> findByStatusOrderByCollectedAtDesc(String status);
}
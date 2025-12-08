package com.university.home.repository;

import com.university.home.entity.PreStuSub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PreStuSubRepository extends JpaRepository<PreStuSub, PreStuSub.PreStuSubId> {

    // 1. 신청된 과목 ID들만 중복 제거해서 조회
    // PreStuSub 엔티티의 id 필드 안에 있는 subjectId를 가리킵니다.
    @Query("SELECT DISTINCT p.id.subjectId FROM PreStuSub p")
    List<Long> findDistinctSubjectIds();

    // 2. 특정 과목 신청 인원 수 (id.subjectId 기준)
    long countByIdSubjectId(Long subjectId);

    // 3. 특정 과목 신청 리스트 조회
    List<PreStuSub> findByIdSubjectId(Long subjectId);
}
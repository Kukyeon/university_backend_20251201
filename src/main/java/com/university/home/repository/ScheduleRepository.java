package com.university.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.university.home.entity.Schedule;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // 특정 학사일정 조회 (Optional 사용)
    Optional<Schedule> findById(Long id);

    // 월별 학사일정 조회
    @Query("SELECT s FROM Schedule s WHERE MONTH(s.startDay) = :month")
    List<Schedule> findByMonth(@Param("month") int month);

    // staff 기준 삭제
    void deleteByIdAndStaff_Id(Long id, Long staffId);
}
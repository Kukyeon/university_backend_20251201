package com.university.home.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.CounselingSchedule;
import com.university.home.entity.ScheduleStatus;

public interface CounselingScheduleRepository extends JpaRepository<CounselingSchedule, Long> {
    List<CounselingSchedule> findByProfessorIdAndStatus(Long professorId, ScheduleStatus status);
    List<CounselingSchedule> findByStudentIdAndStatus(Long studentId, ScheduleStatus status);
    
    List<CounselingSchedule> findByProfessorId(Long professorId);
    List<CounselingSchedule> findByStudentId(Long studentId);
    List<CounselingSchedule> findByProfessorIdAndStatusIn(Long professorId, List<ScheduleStatus> statuses);
}
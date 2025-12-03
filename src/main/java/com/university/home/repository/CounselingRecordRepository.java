package com.university.home.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.university.home.entity.CounselingRecord;

public interface CounselingRecordRepository extends JpaRepository<CounselingRecord, Long>, 
JpaSpecificationExecutor<CounselingRecord> {

Optional<CounselingRecord> findByScheduleId(Long scheduleId);
}
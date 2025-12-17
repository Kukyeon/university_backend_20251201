package com.university.home.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.university.home.entity.CounselingRecord;

public interface CounselingRecordRepository extends JpaRepository<CounselingRecord, Long>, 
JpaSpecificationExecutor<CounselingRecord> {

	Optional<CounselingRecord> findByScheduleId(Long scheduleId);
	Optional<CounselingRecord> findByScheduleIdAndStudentId(Long scheduleId, Long studentId);
	
	@Query(value = "SELECT cr FROM CounselingRecord cr JOIN FETCH cr.schedule s WHERE s.professorId = :professorId",
	countQuery = "SELECT count(cr) FROM CounselingRecord cr JOIN cr.schedule s WHERE s.professorId = :professorId")
	Page<CounselingRecord> findAllRecordsWithSchedule(
	@Param("professorId") Long professorId, 
	Pageable pageable);
}
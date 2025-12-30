package com.university.home.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.university.home.entity.AvailabilityStatus;
import com.university.home.entity.ProfessorAvailability;

import jakarta.persistence.LockModeType;

public interface ProfessorAvailabilityRepository extends JpaRepository<ProfessorAvailability, Long> {
  
	// 교수 본인 시간 조회 (열린/닫힌 전체)
    List<ProfessorAvailability> findByProfessorId(Long professorId);
	
    boolean existsByProfessorIdAndStartTimeLessThanAndEndTimeGreaterThanAndActiveTrue(
    	    Long professorId,
    	    LocalDateTime end,
    	    LocalDateTime start
    	);

    boolean existsByProfessorIdAndStartTimeLessThanAndEndTimeGreaterThan(
    	    Long professorId,
    	    LocalDateTime end,
    	    LocalDateTime start
    	    
    	);
    
    List<ProfessorAvailability> findByProfessorIdAndStatusAndActive(
    	    Long professorId,
    	    AvailabilityStatus status,
    	    boolean active
    	);

	List<ProfessorAvailability> findByStatusAndActive(
	    AvailabilityStatus status,
	    boolean active
		);
    	  
	// 교수 본인 시간 조회 (Active=true인 슬롯만)
	List<ProfessorAvailability> findByProfessorIdAndActive(Long professorId, boolean active);
	  
	Optional<ProfessorAvailability> findByProfessorIdAndStartTimeAndEndTimeAndActiveFalse(
	          Long professorId,
	          LocalDateTime start,
	          LocalDateTime end
		);
	  
	// 동시 예약 방지용 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ProfessorAvailability a WHERE a.id = :id")
    Optional<ProfessorAvailability> findByIdWithLock(@Param("id") Long id);

}
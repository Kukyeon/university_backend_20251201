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
	
    /**
     * 겹침 검사: 활성화된 (Active=true) 슬롯만 대상으로 검사
     * 이 메서드를 CounselingScheduleService.setAvailability에서 사용해야 합니다.
     */
    boolean existsByProfessorIdAndStartTimeLessThanAndEndTimeGreaterThanAndActiveTrue(
    	    Long professorId,
    	    LocalDateTime end,
    	    LocalDateTime start
    	);

    /**
     * 기존의 겹침 검사 (Active 상태를 고려하지 않음) - 현재는 사용하지 않도록 권장
     */
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
    	  
    	// 교수 본인 시간 조회 (Active=true인 슬롯만) - 교수 캘린더 조회에 사용됨
    	List<ProfessorAvailability> findByProfessorIdAndActive(Long professorId, boolean active);
    	  
    	  /**
    	   * ⭐️ 닫힌 슬롯을 찾아 재활성화하기 위한 쿼리 (Active=false)
    	   * CounselingScheduleService.setAvailability에서 사용됩니다.
    	   */
    	  Optional<ProfessorAvailability> findByProfessorIdAndStartTimeAndEndTimeAndActiveFalse(
    	          Long professorId,
    	          LocalDateTime start,
    	          LocalDateTime end
    	  );
    	  
    	  // ⭐️ 동시 예약 방지용 락
    	    @Lock(LockModeType.PESSIMISTIC_WRITE)
    	    @Query("SELECT a FROM ProfessorAvailability a WHERE a.id = :id")
    	    Optional<ProfessorAvailability> findByIdWithLock(@Param("id") Long id);

}
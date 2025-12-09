package com.university.home.controller;

import com.university.home.dto.AvailabilityRequestDto;
import com.university.home.dto.BookingRequestDto;
import com.university.home.dto.RecordSearchRequestDto;
import com.university.home.service.CounselingScheduleService;
import com.university.home.service.CustomUserDetails;
import com.university.home.service.CounselingRecordService;
import com.university.home.entity.ProfessorAvailability;
import com.university.home.entity.ScheduleStatus;
import com.university.home.exception.CustomRestfullException;
import com.university.home.entity.CounselingSchedule;
import com.university.home.entity.CounselingRecord;
import lombok.RequiredArgsConstructor;
import com.university.home.dto.PrincipalDto; 
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class CounselingController {

    private final CounselingScheduleService scheduleService;
    private final CounselingRecordService recordService;
    
    // TODO: 실제 Spring Security에서 현재 로그인 사용자 ID를 가져오는 메서드로 대체해야 합니다.
//    private Long getCurrentUserId() { return 1L; } // 임시 ID 반환

    
//    private Long getCurrentUserId() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new CustomRestfullException("인증된 사용자 정보가 없습니다.", HttpStatus.UNAUTHORIZED);
//        }
    
    // =========================================================
    // 1. 교수자별 상담 가능 시간 설정 및 예약 현황 조회
    // =========================================================
    
    // POST /api/schedules/availability : 교수자 상담 가능 시간 설정
    @PostMapping("/availability")
    public ResponseEntity<ProfessorAvailability> setAvailability(@RequestBody AvailabilityRequestDto request,
    		@AuthenticationPrincipal CustomUserDetails principal) {
       
    	if (principal == null) {
            throw new CustomRestfullException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
    	
    	Long professorId = principal.getUser().getId();
    	
        ProfessorAvailability availability = scheduleService.setAvailability(
            professorId, 
            request.getStartTime(), 
            request.getEndTime()
        );
        return ResponseEntity.ok(availability);
    }
    
    // GET /api/schedules/professor/{profId} : 교수자별 예약 현황 및 가능 시간 조회
    @GetMapping("/professor")
    public ResponseEntity<List<ProfessorAvailability>> getProfessorAvailability(@AuthenticationPrincipal CustomUserDetails principal) {
      
    	if (principal == null) {
            throw new CustomRestfullException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
    	
    	List<ProfessorAvailability> list = scheduleService.getProfessorAvailability(principal.getUser().getId());
        return ResponseEntity.ok(list);
    }
    
    // =========================================================
    // 2. 학생 상담 예약
    // =========================================================
    
    // POST /api/schedules/book : 학생 상담 예약
    @PostMapping("/book")
    public ResponseEntity<CounselingSchedule> bookAppointment(@RequestBody BookingRequestDto request) {
        // request DTO에 studentId와 availabilityId가 포함되어 있어야 함
        CounselingSchedule schedule = scheduleService.bookAppointment(request);
        return ResponseEntity.ok(schedule);
    }

    // =========================================================
    // 3. 상담 일정 변경 및 취소
    // =========================================================
    
    // PUT /api/schedules/cancel/{scheduleId} : 상담 일정 취소
    @PutMapping("/cancel/{scheduleId}")
    public ResponseEntity<CounselingSchedule> cancelAppointment(@PathVariable("scheduleId") Long scheduleId,
    		@AuthenticationPrincipal CustomUserDetails principal) {
    		
    	if (principal == null) {
            throw new CustomRestfullException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
    	
        Long currentUserId = principal.getUser().getId();
        CounselingSchedule cancelledSchedule = scheduleService.cancelAppointment(scheduleId, currentUserId);
        return ResponseEntity.ok(cancelledSchedule);
    }
    
    // =========================================================
    // 4. 학생별 상담 기록 및 검색
    // =========================================================
    
    // GET /api/schedules/student/{studentId} : 학생별 상담 기록 및 저장된 일정 조회
    @GetMapping("/student")
    public ResponseEntity<List<CounselingSchedule>> getStudentSchedules(@AuthenticationPrincipal CustomUserDetails principal) {
      
    	if (principal == null) {
            throw new CustomRestfullException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
    	
    	List<CounselingSchedule> list = scheduleService.getStudentSchedules(principal.getUser().getId());
        return ResponseEntity.ok(list);
    }

 // GET /api/schedules/records/search : 상담 내용 검색 기능
    @GetMapping("/records/search")
    public ResponseEntity<List<CounselingRecord>> searchRecords(RecordSearchRequestDto request) {
        // RecordService를 통해 검색을 수행
        List<CounselingRecord> results = recordService.searchRecords(request);
        return ResponseEntity.ok(results);
    }
    
    // POST /api/schedules/records/save/{scheduleId} : 상담 기록 저장
    // (STT 기능 또는 교수자 수동 메모 저장)
    @PostMapping("/records/save/{scheduleId}")
    public ResponseEntity<CounselingRecord> saveRecord(@PathVariable("scheduleId") Long scheduleId, @RequestBody Map<String, String> body) {
        String notes = body.get("notes"); // STT 결과 또는 메모
        String keywords = body.get("keywords"); // 사용자 입력 키워드 (선택 사항)
        
        CounselingRecord record = recordService.saveRecord(scheduleId, notes, keywords);
        return ResponseEntity.ok(record);
    }

    // GET /api/schedules/records/{scheduleId} : 특정 상담 기록 조회
    @GetMapping("/records/{scheduleId}")
    public ResponseEntity<CounselingRecord> getRecord(@PathVariable("scheduleId") Long scheduleId) {
        CounselingRecord record = recordService.getRecordByScheduleId(scheduleId);
        return ResponseEntity.ok(record);
    }
    
 // ⭐️ GET /api/schedules/requests : 로그인된 교수에게 신청된 상담 일정 조회
    @GetMapping("/requests")
    public ResponseEntity<List<CounselingSchedule>> getProfessorRequests(@AuthenticationPrincipal CustomUserDetails principal) {
    	if (principal == null) {
            throw new CustomRestfullException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
       }
    	
        String userRole = principal.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        Long professorId = principal.getUser().getId();
        
        if (!userRole.toUpperCase().trim().equals("PROFESSOR")) {
            throw new CustomRestfullException("교수만 접근 가능합니다.", HttpStatus.FORBIDDEN);
        }
        // ⭐️ 교수 ID를 서비스로 전달
        List<CounselingSchedule> list = scheduleService.getProfessorRequests(professorId); 
        return ResponseEntity.ok(list);
    }
    
    // ⭐️ PUT /api/schedules/status/{scheduleId} : 상담 일정 상태 변경 (승인/거절/완료)
    @PutMapping("/status/{scheduleId}")
    public ResponseEntity<CounselingSchedule> updateScheduleStatus(
            @PathVariable("scheduleId") Long scheduleId, 
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails principal) {

    	if (principal == null) {
            throw new CustomRestfullException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        
        String userRole = principal.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        
        String statusString = body.get("status");
        if (statusString == null) {
             throw new CustomRestfullException("변경할 상태를 지정해야 합니다.", HttpStatus.BAD_REQUEST);
        }
        ScheduleStatus newStatus = ScheduleStatus.valueOf(statusString);
        
        // ⭐️ 교수 ID와 새 상태를 서비스로 전달
        CounselingSchedule updatedSchedule = scheduleService.updateScheduleStatus(scheduleId, principal.getUser().getId(), newStatus);
        return ResponseEntity.ok(updatedSchedule);
    }
    
 // GET /api/schedules/available-list : 모든 교수님의 예약 가능한 시간 조회
    @GetMapping("/available-list")
    public ResponseEntity<List<ProfessorAvailability>> getAllAvailableTimes() {
        // (인증 필요 없음 또는 단순 조회)
        List<ProfessorAvailability> list = scheduleService.getAllAvailableTimes();
        return ResponseEntity.ok(list);
    }
}
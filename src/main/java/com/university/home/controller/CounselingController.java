package com.university.home.controller;

import com.university.home.dto.AvailabilityRequestDto;
import com.university.home.dto.AvailableTimeResponseDto;
import com.university.home.dto.BookingRequestDto; 
import com.university.home.dto.CounselingRecordResponseDto;
import com.university.home.dto.CounselingScheduleResponseDto;
import com.university.home.dto.EntryValidateDto;
import com.university.home.dto.ProfessorScheduleRequestDto;
import com.university.home.service.CounselingScheduleService;
import com.university.home.service.CounselingRecordService;
import com.university.home.service.CustomUserDetails;
import com.university.home.entity.ProfessorAvailability;
import com.university.home.entity.CounselingSchedule;
import com.university.home.entity.CounselingRecord;
import com.university.home.entity.ScheduleStatus;
import com.university.home.exception.CustomRestfullException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class CounselingController {

    private final CounselingScheduleService scheduleService;
    private final CounselingRecordService counselingRecordService;

    // 💡 공통 Principal 유효성 검사 및 ID 추출 메서드
    private Long getUserId(CustomUserDetails principal, String role) {
        if (principal == null || principal.getUser() == null) {
            throw new CustomRestfullException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        
        Long id = principal.getUser().getId();
        // 💡 필요하다면 여기에 role 체크 로직 추가 가능
        // if (!principal.getUser().getUserRole().name().equalsIgnoreCase(role)) { ... }
        return id;
    }


    // =========================================================
    // 1. 교수 상담 가능 시간
    // =========================================================

    @PostMapping("/availability")
    public ResponseEntity<ProfessorAvailability> setAvailability(
        @RequestBody AvailabilityRequestDto request,
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long professorId = getUserId(principal, "professor");

        return ResponseEntity.ok(
            scheduleService.setAvailability(
                professorId,
                request.getStartTime(),
                request.getEndTime()
            )
        );
    }

    @GetMapping("/professor")
    public ResponseEntity<List<ProfessorAvailability>> getProfessorAvailability(
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long professorId = getUserId(principal, "professor");

        return ResponseEntity.ok(
            scheduleService.getProfessorAvailability(professorId)
        );
    }

    
    @PutMapping("/availability/close/{availabilityId}")
    public ResponseEntity<Void> closeAvailability(
        @PathVariable("availabilityId") Long availabilityId,
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long professorId = getUserId(principal, "professor");

        scheduleService.closeAvailability(
            availabilityId,
            professorId
        );

        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // 2. 학생 상담 예약
    // =========================================================

    @PostMapping("/book")
    public ResponseEntity<CounselingSchedule> bookAppointment(
        @RequestBody BookingRequestDto request, // BookingRequestDto를 그대로 사용
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        // 💡 [수정] AuthenticationPrincipal 유효성 검사
        Long studentId = getUserId(principal, "student");

        return ResponseEntity.ok(
            scheduleService.bookAppointment(
                request, // DTO 객체 전달
                studentId
            )
        );
        // ⚠️ JSON 오류가 계속 발생하면, 다음처럼 수동으로 Long을 추출하도록 임시 수정 가능
        // @RequestBody Map<String, Long> requestBody
        // Long availabilityId = requestBody.get("availabilityId");
        // BookingRequestDto request = new BookingRequestDto();
        // request.setAvailabilityId(availabilityId); // DTO에 Setter가 필요함
        // return ResponseEntity.ok(scheduleService.bookAppointment(request, studentId));
    }

    @PutMapping("/cancel/{scheduleId}")
    public ResponseEntity<CounselingSchedule> cancelAppointment(
        @PathVariable("scheduleId") Long scheduleId,
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long currentUserId = getUserId(principal, "any"); // 교수/학생 모두 취소 가능

        return ResponseEntity.ok(
            scheduleService.cancelAppointment(
                scheduleId,
                currentUserId
            )
        );
    }

    // =========================================================
    // 3. 일정 조회
    // =========================================================

    @GetMapping("/student")
    public ResponseEntity<List<CounselingScheduleResponseDto>> getStudentSchedules(
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long studentId = getUserId(principal, "student");

        return ResponseEntity.ok(
            scheduleService.getStudentSchedules(studentId)
        );
    }

    @GetMapping("/available/professor/{professorId}")
    public ResponseEntity<List<AvailableTimeResponseDto>> getAvailableTimesByProfessor(
        @PathVariable("professorId") Long professorId
    ) {
        // 이 엔드포인트는 비로그인 사용자도 접근할 수 있도록 설계되었으므로 인증 검사 생략
        return ResponseEntity.ok(
            scheduleService.getAvailableTimesByProfessor(professorId)
        );
    }

    // =========================================================
    // 4. 교수 상담 요청 관리
    // =========================================================

    @GetMapping("/requests")
    public ResponseEntity<List<ProfessorScheduleRequestDto>> getProfessorRequests(
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long professorId = getUserId(principal, "professor");

        return ResponseEntity.ok(
            scheduleService.getProfessorRequests(professorId)
        );
    }
    
    @GetMapping("/professor/schedules")
    public ResponseEntity<List<ProfessorScheduleRequestDto>> getProfessorAllSchedules(
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long professorId = getUserId(principal, "professor");

        // Service 계층의 새로운 메서드 호출
        return ResponseEntity.ok(
            scheduleService.getProfessorAllSchedules(professorId)
        );
    }

    @PutMapping("/status/{scheduleId}")
    public ResponseEntity<CounselingSchedule> updateScheduleStatus(
        @PathVariable("scheduleId") Long scheduleId,
        @RequestBody Map<String, String> body,
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long professorId = getUserId(principal, "professor");
        
        // Map에서 status를 추출하고 Enum으로 변환
        ScheduleStatus newStatus = Optional.ofNullable(body.get("status"))
            .map(String::toUpperCase)
            .map(ScheduleStatus::valueOf)
            .orElseThrow(() -> new CustomRestfullException("잘못된 상태 값입니다.", HttpStatus.BAD_REQUEST));
        
        return ResponseEntity.ok(
            scheduleService.updateScheduleStatus(
                scheduleId,
                professorId,
                newStatus
            )
        );
    }

    // =========================================================
    // 5. 상담 기록
    // =========================================================
    
    // ... (상담 기록 관련 코드는 인증 로직만 getUserId로 대체하고 유지)

    @PutMapping("/records/{scheduleId}/memo")
    public ResponseEntity<CounselingRecord> saveRecord(
        @PathVariable("scheduleId") Long scheduleId,
        @RequestBody Map<String, String> body,
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long professorId = getUserId(principal, "professor");

        return ResponseEntity.ok(
            counselingRecordService.saveRecord(
                scheduleId,
                professorId,
                body.get("notes"),
                body.get("keywords")
            )
        );
    }

    @GetMapping("/records/{scheduleId}")
    public ResponseEntity<CounselingRecordResponseDto> getRecordForProfessor(
        @PathVariable("scheduleId") Long scheduleId,
        @RequestParam("studentId") Long studentId,
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long professorId = getUserId(principal, "professor");

        return ResponseEntity.ok(
            counselingRecordService.getRecordForProfessor(
                scheduleId,
                studentId,
                professorId
            )
        );
    }
    @GetMapping("/records/search")
    public ResponseEntity<Page<CounselingRecordResponseDto>> searchRecords(
        @RequestParam(value = "studentName", required = false) String studentName,
        @RequestParam(value = "consultationDate", required = false) String consultationDate,
        @RequestParam(value = "keyword", required = false) String keyword,
        @AuthenticationPrincipal CustomUserDetails principal,
        Pageable pageable // 💡 Pageable 객체를 인자로 받도록 수정
    ) {
        Long professorId = getUserId(principal, "professor");

        // Service 계층의 검색 메서드 호출 (Page<T>를 반환)
        return ResponseEntity.ok(
            counselingRecordService.searchRecords(
                professorId, 
                studentName, 
                consultationDate, 
                keyword,
                pageable // 💡 Pageable 객체 전달
            )
        );
    
    }
    @GetMapping("/records/student/{scheduleId}")
    public ResponseEntity<CounselingRecordResponseDto> getRecordForStudent(
        @PathVariable("scheduleId") Long scheduleId,
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long studentId = getUserId(principal, "student");

        return ResponseEntity.ok(
            counselingRecordService.getRecordForStudent(
                scheduleId,
                studentId
            )
        );
    }

    @GetMapping("/records/list")
    public ResponseEntity<List<CounselingRecordResponseDto>> getProfessorRecordList(
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long professorId = getUserId(principal, "professor");

        return ResponseEntity.ok(
            counselingRecordService.getProfessorRecordList(
                professorId
            )
        );
    }
    
    @GetMapping("/professor/schedules/confirmed") 
    public ResponseEntity<List<CounselingScheduleResponseDto>> getConfirmedSchedules(
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long professorId = getUserId(principal, "professor");
        
        // CONFIRMED 일정만 가져오는 새로운 Service 메서드 호출
        List<CounselingScheduleResponseDto> confirmedList = counselingRecordService.getConfirmedSchedulesForProfessor(professorId); 
        
        return ResponseEntity.ok(confirmedList);
    }
    @GetMapping("/validate-entry/{scheduleId}")
    public ResponseEntity<EntryValidateDto> validateEntry(
        @PathVariable(name = "scheduleId") Long scheduleId,
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long userId = getUserId(principal, "any");

        return ResponseEntity.ok(
            scheduleService.checkCanEnterRoom(scheduleId, userId)
        );
    }
    @GetMapping("/entry-check/{scheduleId}")
    public ResponseEntity<EntryValidateDto> checkEntry(
            @PathVariable(name = "scheduleId") Long scheduleId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long userId = getUserId(principal, "any");
        return ResponseEntity.ok(
            scheduleService.checkCanEnterRoom(scheduleId, userId)
        );
    }

    @PostMapping("/enter/{scheduleId}")
    public ResponseEntity<Void> enterRoom(
            @PathVariable(name = "scheduleId") Long scheduleId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long userId = getUserId(principal, "any");

        scheduleService.enterRoom(scheduleId, userId);

        return ResponseEntity.ok().build();
    }
    @PutMapping("/complete/{scheduleId}")
    public ResponseEntity<Void> completeConsultation(
            @PathVariable(name = "scheduleId") Long scheduleId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long professorId = getUserId(principal, "professor");

        scheduleService.completeConsultation(scheduleId, professorId);

        return ResponseEntity.ok().build();
    }




}
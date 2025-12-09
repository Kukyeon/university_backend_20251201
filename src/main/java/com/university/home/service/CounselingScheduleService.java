package com.university.home.service;

import com.university.home.dto.BookingRequestDto;
import com.university.home.dto.CounselingScheduleResponseDto;
import com.university.home.entity.*;
import com.university.home.repository.ProfessorAvailabilityRepository;
import com.university.home.repository.CounselingScheduleRepository;
import com.university.home.exception.CustomRestfullException;
import com.university.home.service.StudentService; // 학생 이름 조회를 위해 주입
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CounselingScheduleService {
    
    private final ProfessorAvailabilityRepository availabilityRepository;
    private final CounselingScheduleRepository scheduleRepository;
    private final StudentService studentService; // 학생 이름 조회를 위해 사용
    private final NotificationService notificationService;

    // [1] 교수자별 상담 가능 시간 설정
    @Transactional
    public ProfessorAvailability setAvailability(Long professorId, LocalDateTime start, LocalDateTime end) {
        // [보완된 로직] 시작/종료 시간 유효성 검사
        if (start.isAfter(end) || start.isEqual(end)) {
            throw new CustomRestfullException("시작 시간은 종료 시간보다 빨라야 합니다.", HttpStatus.BAD_REQUEST);
        }
        
        ProfessorAvailability availability = new ProfessorAvailability();
        availability.setProfessorId(professorId);
        availability.setStartTime(start);
        availability.setEndTime(end);
        
        return availabilityRepository.save(availability);
    }
    
    // [2] 학생 상담 예약 (핵심 로직)
    @Transactional
    public CounselingSchedule bookAppointment(BookingRequestDto request) {
        ProfessorAvailability availability = availabilityRepository.findById(request.getAvailabilityId())
                .orElseThrow(() -> new CustomRestfullException("해당 가능 시간이 존재하지 않습니다.", HttpStatus.NOT_FOUND));
        
        if (availability.isBooked()) {
            throw new CustomRestfullException("이미 예약된 시간입니다.", HttpStatus.BAD_REQUEST);
        }
        
        // 1) Availability 상태 업데이트 (잠금)
        availability.setBooked(true);
        availabilityRepository.save(availability);
        
        // 2) Schedule 생성
        CounselingSchedule schedule = new CounselingSchedule();
        schedule.setStudentId(request.getStudentId());
        schedule.setProfessorId(availability.getProfessorId());
        schedule.setAvailability(availability);
        schedule.setStartTime(availability.getStartTime());
        schedule.setEndTime(availability.getEndTime());
        schedule.setStatus(ScheduleStatus.CONFIRMED);
        
        notificationService.sendAppointmentAlert(schedule, "예약 완료");
        
        return scheduleRepository.save(schedule);
    }
    
    // [3] 상담 일정 변경 및 취소
    @Transactional
    public CounselingSchedule cancelAppointment(Long scheduleId, Long currentUserId) {
        CounselingSchedule schedule = scheduleRepository.findById(scheduleId)
             .orElseThrow(() -> new CustomRestfullException("해당 상담 일정이 존재하지 않습니다.", HttpStatus.NOT_FOUND));
             
        // 권한 검사 (교수 또는 해당 학생만 취소 가능)
        if (!schedule.getProfessorId().equals(currentUserId) && !schedule.getStudentId().equals(currentUserId)) {
            throw new CustomRestfullException("취소 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
        if (schedule.getStatus() == ScheduleStatus.CANCELED) {
            throw new CustomRestfullException("이미 취소된 일정입니다.", HttpStatus.BAD_REQUEST);
        }
             
        // 1) Availability 상태를 예약 가능으로 되돌림
        ProfessorAvailability availability = schedule.getAvailability();
        availability.setBooked(false);
        availabilityRepository.save(availability);
        
        // 2) Schedule 상태 변경
        schedule.setStatus(ScheduleStatus.CANCELED);
        scheduleRepository.save(schedule);
        
        notificationService.sendAppointmentAlert(schedule, "예약 취소");
        
        return scheduleRepository.save(schedule);
    }
    
    // [4] 교수자별 예약 현황 조회
    public List<ProfessorAvailability> getProfessorAvailability(Long professorId) {
        // 예약된 시간과 예약 가능한 시간을 모두 조회하여 캘린더에 표시할 수 있도록 반환합니다.
        return availabilityRepository.findAll().stream()
                .filter(a -> a.getProfessorId().equals(professorId))
                .toList();
    }
    
    // [5] 학생별 상담 기록 및 저장된 일정 조회
    public List<CounselingScheduleResponseDto> getStudentSchedules(Long studentId) {
        return scheduleRepository.findByStudentId(studentId)
        		.stream()
        		.map(schedule -> {
        			
        			String professorName = studentService.getProfessorName(schedule.getProfessorId());
        			String studentName = studentService.getStudentName(schedule.getStudentId());
        			return new CounselingScheduleResponseDto(schedule, professorName, studentName);
                })
                .toList();
    }
    
 // [6] 교수에게 신청된 모든 상담 요청 조회
    public List<CounselingSchedule> getProfessorRequests(Long professorId) {
        // CONFIRMED(승인 대기) 또는 COMPLETED(완료)된 일정을 모두 조회
        return scheduleRepository.findByProfessorId(professorId); 
    }
    
    // [7] 상담 일정 상태 변경 (교수 전용)
    @Transactional
    public CounselingSchedule updateScheduleStatus(Long scheduleId, Long professorId, ScheduleStatus newStatus) {
        CounselingSchedule schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new CustomRestfullException("해당 상담 일정이 존재하지 않습니다.", HttpStatus.NOT_FOUND));

        // 권한 검사: 해당 상담의 교수가 현재 로그인한 교수인지 확인
        if (!schedule.getProfessorId().equals(professorId)) {
            throw new CustomRestfullException("해당 상담의 상태를 변경할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        schedule.setStatus(newStatus);
        
        // 상태가 CANCELED로 바뀌면, Availability를 다시 예약 가능 상태로 돌려놓아야 함
        if (newStatus == ScheduleStatus.CANCELED) {
            ProfessorAvailability availability = schedule.getAvailability();
            availability.setBooked(false);
            availabilityRepository.save(availability);
            notificationService.sendAppointmentAlert(schedule, "예약 취소됨");
        } else if (newStatus == ScheduleStatus.COMPLETED) {
            notificationService.sendAppointmentAlert(schedule, "상담 완료됨");
        }
        
        return scheduleRepository.save(schedule);
    }
    
    public List<ProfessorAvailability> getAllAvailableTimes() {
        // isBooked가 false인 모든 Availability를 조회
        return availabilityRepository.findByIsBooked(false);
    }
}
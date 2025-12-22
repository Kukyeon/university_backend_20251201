
package com.university.home.service;

import com.university.home.dto.AvailableTimeResponseDto;
import com.university.home.dto.BookingRequestDto;
import com.university.home.dto.CounselingScheduleResponseDto;
import com.university.home.dto.EntryValidateDto;
import com.university.home.dto.ProfessorScheduleRequestDto;
import com.university.home.entity.*;
import com.university.home.repository.ProfessorAvailabilityRepository;
import com.university.home.repository.CounselingScheduleRepository;
import com.university.home.exception.CustomRestfullException;
import com.university.home.service.StudentService; // 학생 이름 조회를 위해 주입
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CounselingScheduleService {
    
    private final ProfessorAvailabilityRepository availabilityRepository;
    private final CounselingScheduleRepository scheduleRepository;
    private final StudentService studentService; // 학생 이름 조회를 위해 사용
    private final NotificationService notificationService;


        // [1] 교수 상담 가능 시간 등록
    @Transactional
    public ProfessorAvailability setAvailability(
        Long professorId,
        LocalDateTime start,
        LocalDateTime end
    ) {

        if (!start.isBefore(end)) {
            throw new CustomRestfullException(
                "시작 시간은 종료 시간보다 빨라야 합니다.",
                HttpStatus.BAD_REQUEST
            );
        }
        
        // 💡 [추가] 닫힌 슬롯을 다시 여는 경우 (재활성화 로직)
        Optional<ProfessorAvailability> existingClosedOpt = 
            availabilityRepository.findByProfessorIdAndStartTimeAndEndTimeAndActiveFalse(
                professorId, start, end
            );

        if (existingClosedOpt.isPresent()) {
            ProfessorAvailability existingClosed = existingClosedOpt.get();
            // 닫혀있던 슬롯을 OPEN 상태로 재활성화 (업데이트)
            existingClosed.setActive(true);
            existingClosed.setStatus(AvailabilityStatus.OPEN);
            return availabilityRepository.save(existingClosed);
        }

        // 2. 겹침(Overlap) 검사: 활성화된 슬롯만 대상으로 검사하도록 로직 변경
        boolean overlap =
            availabilityRepository
                .existsByProfessorIdAndStartTimeLessThanAndEndTimeGreaterThanAndActiveTrue( // 💡 [수정] Active=true 조건 추가
                    professorId,
                    end,
                    start
                );

        if (overlap) {
            // 이 에러는 활성화된 슬롯과 겹치는 경우에만 발생해야 합니다.
            throw new CustomRestfullException(
                "이미 등록된 시간과 겹칩니다.",
                HttpStatus.BAD_REQUEST
            );
        }

        // 3. 완전히 새로운 슬롯 등록 (기존 코드가 이 위치로 이동)
        ProfessorAvailability availability = new ProfessorAvailability();
        availability.setProfessorId(professorId);
        availability.setStartTime(start);
        availability.setEndTime(end);
        availability.setStatus(AvailabilityStatus.OPEN);
        availability.setActive(true);

        return availabilityRepository.save(availability);
    }

        // [2] 학생 상담 예약
        @Transactional
        public CounselingSchedule bookAppointment(
            BookingRequestDto request,
            Long studentId
        ) {

        	ProfessorAvailability availability =
        		    availabilityRepository.findByIdWithLock(request.getAvailabilityId())
        		        .orElseThrow(() ->
        		            new CustomRestfullException("예약 가능한 시간이 아닙니다.", HttpStatus.NOT_FOUND)
        		        );

            if (availability.getStatus() != AvailabilityStatus.OPEN || !availability.isActive()) {
                throw new CustomRestfullException(
                    "이미 예약되었거나 사용할 수 없는 시간입니다.",
                    HttpStatus.CONFLICT
                );
            }

            availability.setStatus(AvailabilityStatus.REQUESTED);
            availabilityRepository.save(availability);

            CounselingSchedule schedule = new CounselingSchedule();
            schedule.setProfessorId(availability.getProfessorId());
            schedule.setStudentId(studentId);
            schedule.setAvailability(availability);
            schedule.setStartTime(availability.getStartTime());
            schedule.setEndTime(availability.getEndTime());
            schedule.setStatus(ScheduleStatus.PENDING);

            notificationService.sendAppointmentAlert(schedule, "예약");
            return scheduleRepository.save(schedule);
        }

        // [3] 상담 취소
        @Transactional
        public CounselingSchedule cancelAppointment(
            Long scheduleId,
            Long currentUserId
        ) {

            CounselingSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() ->
                    new CustomRestfullException("해당 상담 일정이 존재하지 않습니다.", HttpStatus.NOT_FOUND)
                );

            if (!schedule.getProfessorId().equals(currentUserId)
                && !schedule.getStudentId().equals(currentUserId)) {
                throw new CustomRestfullException("취소 권한이 없습니다.", HttpStatus.FORBIDDEN);
            }

            if (schedule.getStatus() == ScheduleStatus.CANCELED) {
                throw new CustomRestfullException("이미 취소된 일정입니다.", HttpStatus.BAD_REQUEST);
            }
            
            if (schedule.getStatus() == ScheduleStatus.COMPLETED) {
                throw new CustomRestfullException("이미 완료된 상담은 취소할 수 없습니다.", HttpStatus.BAD_REQUEST);
            }
            
            ProfessorAvailability availability = schedule.getAvailability();
            availability.setStatus(AvailabilityStatus.OPEN);
            availabilityRepository.save(availability);

            schedule.setStatus(ScheduleStatus.CANCELED);
            scheduleRepository.save(schedule);

            notificationService.sendAppointmentAlert(schedule, "예약 취소");

            return schedule;
        }

        // [4] 교수 캘린더 조회
        public List<ProfessorAvailability> getProfessorAvailability(Long professorId) {
            return availabilityRepository.findByProfessorIdAndActive(professorId, true);
        }

        // [5] 학생 상담 일정 조회
        @Transactional
        public List<CounselingScheduleResponseDto> getStudentSchedules(Long studentId) {
        	List<CounselingSchedule> schedules =
        	        scheduleRepository.findByStudentId(studentId);

        	    schedules.forEach(this::applyNoShowIfNeeded);

        	    return schedules.stream()
        	        .map(s -> new CounselingScheduleResponseDto(
        	            s,
        	            studentService.getProfessorName(s.getProfessorId()),
        	            studentService.getStudentName(studentId)
        	        ))
        	        .toList();
        	}

        // [6] 교수 상담 요청 목록
        public List<ProfessorScheduleRequestDto> getProfessorRequests(Long professorId) {
            return scheduleRepository
                .findByProfessorIdAndStatus(professorId, ScheduleStatus.PENDING)
                .stream()
                .map(s -> new ProfessorScheduleRequestDto(
                    s,
                    studentService.getStudentName(s.getStudentId())
                ))
                .toList();
        }

        public List<ProfessorScheduleRequestDto> getProfessorAllSchedules(Long professorId) {
            return scheduleRepository
                // PENDING, CONFIRMED, COMPLETED 상태의 일정을 모두 가져옵니다.
                // Repository에 findByProfessorIdAndStatusIn(Long professorId, List<ScheduleStatus> statuses) 필요
                .findByProfessorId(professorId) // 모든 일정을 가져와 필터링하거나, Repository에서 필터링
                .stream()
                .filter(s -> s.getStatus() != ScheduleStatus.CANCELED) // 취소된 일정은 제외
                .map(s -> new ProfessorScheduleRequestDto(
                    s,
                    studentService.getStudentName(s.getStudentId())
                ))
                .toList();
        }
        
        
        // [7] 상담 상태 변경 (교수)
        @Transactional
        public CounselingSchedule updateScheduleStatus(
            Long scheduleId,
            Long professorId,
            ScheduleStatus newStatus
        ) {

            CounselingSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() ->
                    new CustomRestfullException("상담 일정 없음", HttpStatus.NOT_FOUND)
                );

            if (!schedule.getProfessorId().equals(professorId)) {
                throw new CustomRestfullException("권한 없음", HttpStatus.FORBIDDEN);
            }

            ProfessorAvailability availability = schedule.getAvailability();

            if (newStatus == ScheduleStatus.CONFIRMED) {
                availability.setStatus(AvailabilityStatus.CLOSED);
            }

            if (newStatus == ScheduleStatus.CANCELED) {
                availability.setStatus(AvailabilityStatus.OPEN);
            }

            availabilityRepository.save(availability);
            schedule.setStatus(newStatus);

            return scheduleRepository.save(schedule);
        }

        // [8] 학생 예약용 시간 조회
        public List<AvailableTimeResponseDto> getAvailableTimesByProfessor(Long professorId) {
            return availabilityRepository
                .findByProfessorIdAndStatusAndActive(
                    professorId,
                    AvailabilityStatus.OPEN,
                    true
                )
                .stream()
                .map(a -> new AvailableTimeResponseDto(
                    a.getId(),
                    a.getProfessorId(),
                    studentService.getProfessorName(a.getProfessorId()),
                    a.getStartTime(),
                    a.getEndTime(),
                    a.getStatus().name()
                ))
                .toList();
        }

        // [9] 시간 비활성화
        @Transactional
        public void closeAvailability(Long availabilityId, Long professorId) {

            ProfessorAvailability availability =
                availabilityRepository.findById(availabilityId)
                    .orElseThrow(() ->
                        new CustomRestfullException("시간 없음", HttpStatus.NOT_FOUND)
                    );

            if (!availability.getProfessorId().equals(professorId)) {
                throw new CustomRestfullException("권한 없음", HttpStatus.FORBIDDEN);
            }

            availability.setActive(false);
            availabilityRepository.save(availability);
        }
        @Transactional
        protected void applyNoShowIfNeeded(CounselingSchedule schedule) {
            if (schedule.getStatus() == ScheduleStatus.CONFIRMED
                && LocalDateTime.now().isAfter(schedule.getEndTime())) {

                schedule.setStatus(ScheduleStatus.NO_SHOW);
            }
        }
        
        public void validateCanEnterRoom(CounselingSchedule schedule, Long userId) {
            LocalDateTime now = LocalDateTime.now();

            if (schedule.getStatus() == ScheduleStatus.PENDING
                    || schedule.getStatus() == ScheduleStatus.CANCELED
                    || schedule.getStatus() == ScheduleStatus.COMPLETED
                    || schedule.getStatus() == ScheduleStatus.NO_SHOW) {

                    throw new CustomRestfullException("입장할 수 없는 상담 상태입니다.", HttpStatus.BAD_REQUEST);
                }

                if (now.isBefore(schedule.getStartTime())) {
                    throw new CustomRestfullException("아직 상담 시작 시간이 아닙니다.", HttpStatus.BAD_REQUEST);
                }

                if (now.isAfter(schedule.getEndTime())) {
                    throw new CustomRestfullException("상담 시간이 종료되었습니다.", HttpStatus.BAD_REQUEST);
                }

                if (!schedule.getStudentId().equals(userId)
                    && !schedule.getProfessorId().equals(userId)) {
                    throw new CustomRestfullException("권한이 없습니다.", HttpStatus.FORBIDDEN);
                }
        }
        public EntryValidateDto checkCanEnterRoom(Long scheduleId, Long userId) {

            CounselingSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() ->
                    new CustomRestfullException("상담 일정이 존재하지 않습니다.", HttpStatus.NOT_FOUND)
                );
            applyNoShowIfNeeded(schedule);
            if (!schedule.getStudentId().equals(userId)
                && !schedule.getProfessorId().equals(userId)) {
                return EntryValidateDto.fail("권한이 없습니다.");
            }

            LocalDateTime now = LocalDateTime.now();

            // 상태 체크
            if (schedule.getStatus() == ScheduleStatus.PENDING) {
                return EntryValidateDto.fail("교수 확인 대기 중입니다.");
            }

            if (schedule.getStatus() == ScheduleStatus.CANCELED) {
                return EntryValidateDto.fail("취소된 상담입니다.");
            }

            if (schedule.getStatus() == ScheduleStatus.COMPLETED) {
                return EntryValidateDto.fail("이미 종료된 상담입니다.");
            }
            if (schedule.getStatus() == ScheduleStatus.NO_SHOW) {
                return EntryValidateDto.fail("노쇼 처리된 상담입니다.");
            }

            // 시간 체크
            if (now.isBefore(schedule.getStartTime())) {
                return EntryValidateDto.fail("아직 상담 시작 시간이 아닙니다.");
            }

            if (now.isAfter(schedule.getEndTime())) {
                return EntryValidateDto.fail("상담 시간이 종료되었습니다.");
            }

            return EntryValidateDto.ok();
        }
        @Transactional
        public void enterRoom(Long scheduleId, Long userId) {
            CounselingSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomRestfullException("상담 일정 없음", HttpStatus.NOT_FOUND));
            applyNoShowIfNeeded(schedule);
            validateCanEnterRoom(schedule, userId);

            if (schedule.getStatus() == ScheduleStatus.CONFIRMED) {
                schedule.setStatus(ScheduleStatus.IN_PROGRESS);
            } else if (schedule.getStatus() == ScheduleStatus.IN_PROGRESS) {
                return; // 이미 입장한 상태
            }

        }
        @Transactional
        public void completeConsultation(Long scheduleId, Long professorId) {
            CounselingSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomRestfullException("상담 일정 없음", HttpStatus.NOT_FOUND));

            if (!schedule.getProfessorId().equals(professorId)) {
                throw new CustomRestfullException("권한 없음", HttpStatus.FORBIDDEN);
            }

            if (schedule.getStatus() != ScheduleStatus.IN_PROGRESS) {
                throw new CustomRestfullException("진행 중인 상담만 완료할 수 있습니다.", HttpStatus.BAD_REQUEST);
            }

            schedule.setStatus(ScheduleStatus.COMPLETED);
            scheduleRepository.save(schedule);
        }


    }


package com.university.home.service;

import com.university.home.dto.RecordSearchRequestDto;
import com.university.home.entity.CounselingRecord;
import com.university.home.entity.CounselingSchedule;
import com.university.home.entity.ScheduleStatus;
import com.university.home.repository.CounselingRecordRepository;
import com.university.home.repository.CounselingScheduleRepository;
import com.university.home.exception.CustomRestfullException;
import com.university.home.service.StudentService; // 학생 이름 조회를 위해 가정
import com.university.home.RecordSpecification; // ⭐ 검색 로직을 위한 클래스
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CounselingRecordService {
    
    private final CounselingRecordRepository recordRepository;
    private final CounselingScheduleRepository scheduleRepository;
    private final StudentService studentService; // 학생 이름 조회를 위해 사용

    // [1] 상담 기록 저장 (STT 완료 또는 교수자 메모 입력 시)
    @Transactional
    public CounselingRecord saveRecord(Long scheduleId, String notes, String keywords) {
        CounselingSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomRestfullException("상담 일정이 존재하지 않아 기록할 수 없습니다.", HttpStatus.NOT_FOUND));

        // 이미 기록이 있다면 업데이트, 없다면 새로 생성
        Optional<CounselingRecord> existingRecord = recordRepository.findByScheduleId(scheduleId);
        
        CounselingRecord record = existingRecord.orElseGet(CounselingRecord::new);
        
        // --- 학생 정보 및 일정 정보 매핑 ---
        // TODO: studentService.getStudentById(schedule.getStudentId())를 통해 학생 이름 획득
        String studentName = "테스트 학생 이름"; // 임시 값
        
        record.setSchedule(schedule);
        record.setStudentId(schedule.getStudentId());
        record.setStudentName(studentName);
        record.setConsultationDate(schedule.getStartTime());
        
        // --- 기록 내용 업데이트 ---
        record.setNotes(notes);
        record.setKeywords(keywords); 
        record.setRecordDate(LocalDateTime.now());
        
        // 상담 일정을 완료 상태로 변경 (선택 사항)
        schedule.setStatus(ScheduleStatus.COMPLETED);
        scheduleRepository.save(schedule);
        
        return recordRepository.save(record);
    }

    // [2] 상담 내용 검색 (학생 이름, 상담 날짜, 키워드 등)
    public List<CounselingRecord> searchRecords(RecordSearchRequestDto request) {
        
        // RecordSpecification 클래스를 사용하여 동적 Specification을 구축합니다.
    	Specification<CounselingRecord> spec = Specification.where((Specification<CounselingRecord>) null);

        // 1. 학생 이름 검색
        if (request.getStudentName() != null && !request.getStudentName().isEmpty()) {
            // 이후 .and() 체이닝은 정상 작동합니다.
            spec = spec.and(RecordSpecification.hasStudentName(request.getStudentName()));
        }

        // 2. 날짜 검색
        if (request.getConsultationDate() != null && !request.getConsultationDate().isEmpty()) {
            try {
                // request.getConsultationDate() (String)을 LocalDate로 파싱하여 searchDate 변수 선언 및 초기화
                LocalDate searchDate = LocalDate.parse(request.getConsultationDate(), DateTimeFormatter.ISO_DATE); 
                spec = spec.and(RecordSpecification.hasConsultationDate(searchDate));
            } catch (Exception e) {
                // 날짜 형식이 YYYY-MM-DD가 아닐 경우 처리
                throw new CustomRestfullException("날짜 형식이 올바르지 않습니다. (YYYY-MM-DD)", HttpStatus.BAD_REQUEST);
            }
        }
        
        // 3. 키워드/내용 검색
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            spec = spec.and(RecordSpecification.containsKeyword(request.getKeyword()));
        }

        return recordRepository.findAll(spec);
    }
    
    // [3] 특정 상담 기록 조회
    public CounselingRecord getRecordByScheduleId(Long scheduleId) {
        return recordRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new CustomRestfullException("기록된 상담 내용이 없습니다.", HttpStatus.NOT_FOUND));
    }
}
package com.university.home.service;

import com.university.home.dto.CounselingRecordResponseDto;
import com.university.home.dto.CounselingScheduleResponseDto;
import com.university.home.dto.RecordSearchRequestDto;
import com.university.home.entity.CounselingRecord;
import com.university.home.entity.CounselingSchedule;
import com.university.home.entity.Professor;
import com.university.home.entity.ScheduleStatus;
import com.university.home.entity.Student;
import com.university.home.repository.CounselingRecordRepository;
import com.university.home.repository.CounselingScheduleRepository;
import com.university.home.repository.ProfessorRepository;
import com.university.home.repository.StudentRepository;
import com.university.home.exception.CustomRestfullException;
import com.university.home.service.StudentService; // 학생 이름 조회를 위해 가정
import com.university.home.RecordSpecification; // ⭐ 검색 로직을 위한 클래스
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
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
	@Autowired
    private final CounselingRecordRepository recordRepository;
	
	private final StudentRepository studentRepository;
    private final CounselingScheduleRepository scheduleRepository;
    private final StudentService studentService; // 학생 이름 조회를 위해 사용
    
    @Autowired
    private ProfessorRepository professorRepository;
    
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
        String studentName = studentRepository.findById(schedule.getStudentId())
                .map(Student::getName)
                .orElse("알 수 없는 학생"); // 임시 값
        
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
    public CounselingRecord getRecordByScheduleId(Long scheduleId, Long studentId) {
        return recordRepository.findByScheduleIdAndStudentId(scheduleId, studentId)
                .orElseThrow(() -> new CustomRestfullException("기록된 상담 내용이 없습니다.", HttpStatus.NOT_FOUND));
    }
    
    public CounselingRecordResponseDto getRecordForStudent(Long scheduleId, Long studentId) {

        // 1. 상담 일정(Schedule) 조회 및 학생 권한 검사 (추가)
        CounselingSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomRestfullException("상담 일정이 존재하지 않습니다.", HttpStatus.NOT_FOUND));

        if (!schedule.getStudentId().equals(studentId)) {
            throw new CustomRestfullException("해당 상담 일정을 조회할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        // 2. 교수 정보 조회 및 이름 추출 (기존 로직 유지)
        Long professorId = schedule.getProfessorId(); 
        Professor professor = professorRepository.findById(professorId)
            .orElseThrow(() -> new CustomRestfullException("교수 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        String professorName = professor.getName(); 

        // 3. 학생 이름 조회 (Record에 의존하지 않고 Schedule에서 학생 ID로 조회)
        // TODO: studentService.getStudentById(studentId)에서 실제 학생 이름 획득 로직 필요
        String studentName = "테스트 학생 이름"; // 임시 값

        // 4. Record 조회 (Optional 사용)
        Optional<CounselingRecord> optionalRecord = recordRepository.findByScheduleIdAndStudentId(scheduleId, studentId);

        // 5. Schedule DTO 생성 (Record 유무와 관계없이 공통)
        CounselingScheduleResponseDto scheduleDto = new CounselingScheduleResponseDto(
            schedule, 
            professorName, 
            studentName
        );

        // 6. Record 존재 유무에 따른 응답 처리 ⭐ 이 부분이 수정의 핵심입니다.
        if (optionalRecord.isEmpty()) {
            // 상담 기록이 없을 경우: Schedule 정보만 담은 응답 DTO 반환
            return CounselingRecordResponseDto.fromEmptyRecord(scheduleDto, studentName, studentId); 
        } else {
            // 상담 기록이 있을 경우: 정상적으로 Record 정보 포함하여 반환
            CounselingRecord record = optionalRecord.get();
            return CounselingRecordResponseDto.fromEntity(record, scheduleDto); 
        }
    }
    
    @Transactional(readOnly = true)
    public CounselingRecordResponseDto getRecordForProfessor(Long scheduleId, Long studentId, Long professorId) {

        // 1. 상담 일정(Schedule) 조회 및 교수 권한 검사
        CounselingSchedule schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new CustomRestfullException("상담 일정이 존재하지 않습니다.", HttpStatus.NOT_FOUND));

        if (!schedule.getProfessorId().equals(professorId)) {
            throw new CustomRestfullException("해당 상담 기록을 조회할 권한이 없습니다. (담당 교수가 아님)", HttpStatus.FORBIDDEN);
        }
        
        // 2. 교수 정보 조회 및 이름 추출
        Professor professor = professorRepository.findById(schedule.getProfessorId())
            .orElseThrow(() -> new CustomRestfullException("교수 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        String professorName = professor.getName(); 
        
        // 3. 학생 이름 조회 (Service 또는 Repositoy를 통해 실제 학생 이름 조회)
        // 이 부분은 실제 구현에 따라 달라지며, StudentService가 필요합니다.
        // 임시로 studentId를 기반으로 이름을 조회하거나, schedule에서 얻는다고 가정합니다.
        String studentName;
        try {
            // 실제 학생 서비스 호출 (Student 엔티티를 조회하여 이름을 가져옴)
            studentName = studentRepository.findById(studentId) 
                .map(Student::getName)
                .orElse("학생 정보 조회 실패");
        } catch (Exception e) {
            studentName = "학생 정보 조회 실패";
        }

        // 4. CounselingRecord 조회 (Optional로 처리)
        // ⭐ 현재 오류가 발생하고 있는 부분: Optional.orElseThrow 대신 Optional을 그대로 사용
        Optional<CounselingRecord> optionalRecord = recordRepository.findByScheduleIdAndStudentId(scheduleId, studentId);

        // 5. Schedule DTO 생성 (Record 유무와 관계없이 공통)
        CounselingScheduleResponseDto scheduleDto = new CounselingScheduleResponseDto(
            schedule, 
            professorName, 
            studentName
        );

        // 6. Record 존재 유무에 따른 응답 처리 ⭐
        if (optionalRecord.isEmpty()) {
            // Record가 없을 경우: 일정 정보만 포함하고 내용(notes, keywords)은 비운 DTO 반환
            return CounselingRecordResponseDto.fromEmptyRecord(scheduleDto, studentName, studentId); 
        } else {
            // Record가 존재할 경우: 정상적으로 Record 정보 포함하여 반환
            CounselingRecord record = optionalRecord.get();
            return CounselingRecordResponseDto.fromEntity(record, scheduleDto); 
        }
    }
}
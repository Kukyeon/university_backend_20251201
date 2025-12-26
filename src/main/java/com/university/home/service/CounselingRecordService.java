package com.university.home.service;

import com.university.home.dto.CounselingRecordResponseDto;
import com.university.home.dto.CounselingScheduleResponseDto;
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
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
	
	private final StudentRepository studentRepository;
    private final CounselingScheduleRepository scheduleRepository;
    private final StudentService studentService; 
    
    @Autowired
    private ProfessorRepository professorRepository;
    
    // 상담 기록 저장 (STT 완료 또는 교수자 메모 입력 시)
    @Transactional
    public CounselingRecord saveRecord(Long scheduleId,Long professorId, String notes, String keywords) {
        CounselingSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomRestfullException("상담 일정이 존재하지 않아 기록할 수 없습니다.", HttpStatus.NOT_FOUND));

        if (!schedule.getProfessorId().equals(professorId)) {
            throw new CustomRestfullException("해당 상담 기록을 저장/수정할 권한이 없습니다. (담당 교수가 아님)", HttpStatus.FORBIDDEN);
        }
        
        Optional<CounselingRecord> existingRecord = recordRepository.findByScheduleId(scheduleId);
        CounselingRecord record = existingRecord.orElseGet(CounselingRecord::new);
        
        String studentName = studentRepository.findById(schedule.getStudentId())
                .map(Student::getName)
                .orElse("알 수 없는 학생"); 
        
        record.setSchedule(schedule);
        record.setStudentId(schedule.getStudentId());
        record.setStudentName(studentName);
        record.setConsultationDate(schedule.getStartTime());
        
        record.setNotes(notes);
        record.setKeywords(keywords); 
        record.setRecordDate(LocalDateTime.now());
        
        // 상담 일정을 완료 상태로 변경 
        schedule.setStatus(ScheduleStatus.COMPLETED);
        scheduleRepository.save(schedule);
        
        return recordRepository.save(record);
    }
    
    //  완료된 상담 기록 목록 조회 (페이지네이션/검색이 없는 단순 목록)
    public List<CounselingRecordResponseDto> getProfessorRecordList(Long professorId) {
        // 해당 교수의 완료된(COMPLETED) 상담 일정을 모두 조회
        List<CounselingSchedule> completedSchedules = scheduleRepository.findByProfessorIdAndStatus(professorId, ScheduleStatus.COMPLETED);
        
        return completedSchedules.stream()
                .map(schedule -> {
                    // Schedule ID로 해당 Record를 조회 (Optional)
                    Optional<CounselingRecord> optionalRecord = recordRepository.findByScheduleId(schedule.getId());
                    
                    String professorName = professorRepository.findById(professorId).map(Professor::getName).orElse("교수");
                    String studentName = studentRepository.findById(schedule.getStudentId()).map(Student::getName).orElse("학생");
                    
                    CounselingScheduleResponseDto scheduleDto = new CounselingScheduleResponseDto(
                        schedule, 
                        professorName, 
                        studentName
                    );
                    
                    if (optionalRecord.isPresent()) {
                        return CounselingRecordResponseDto.fromEntity(optionalRecord.get(), scheduleDto);
                    } else {
                        return CounselingRecordResponseDto.fromEmptyRecord(scheduleDto, studentName, schedule.getStudentId());
                    }
                })
                .toList();
    }
    
    // 상담 내용 검색 (학생 이름, 상담 날짜, 키워드 등) 
    @Transactional(readOnly = true)
    public Page<CounselingRecordResponseDto> searchRecords(
        Long professorId, 
        String studentName, 
        String consultationDateStr, 
        String keyword,
        Pageable pageable 
    ) {
        
        //  Specification 구성
    	Specification<CounselingRecord> spec = Specification.where((root, query, builder) -> 
        builder.equal(root.get("schedule").get("professorId"), professorId)
    );

        // 학생 이름 검색 
        if (studentName != null && !studentName.trim().isEmpty()) {
            List<Long> studentIds = studentRepository.findByNameContainingIgnoreCase(studentName.trim())
                                            .stream()
                                            .map(Student::getId)
                                            .toList();
            
            if (studentIds.isEmpty()) {
                return Page.empty(pageable);
            }

            spec = spec.and((root, query, builder) -> 
                root.get("studentId").in(studentIds)
            );
        }
        
        // 날짜 검색
        if (consultationDateStr != null && !consultationDateStr.trim().isEmpty()) {
            try {
                LocalDate searchDate = LocalDate.parse(consultationDateStr.trim(), DateTimeFormatter.ISO_DATE); 
                
                spec = spec.and((root, query, builder) -> 
                    builder.between(
                        root.get("consultationDate"),
                        searchDate.atStartOfDay(),
                        searchDate.plusDays(1).atStartOfDay().minusNanos(1)
                    )
                );
            } catch (Exception e) {
                throw new CustomRestfullException("날짜 형식이 올바르지 않습니다. (YYYY-MM-DD 형식만 허용됩니다)", HttpStatus.BAD_REQUEST);
            }
        }
        
        // 키워드/내용 검색
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
            
            spec = spec.and((root, query, builder) -> 
                builder.or(
                    builder.like(builder.lower(root.get("notes")), likeKeyword),
                    builder.like(builder.lower(root.get("keywords")), likeKeyword)
                )
            );
        }

        // 최종 검색 실행
        Page<CounselingRecord> recordPage = recordRepository.findAll(spec, pageable);
        
        // DTO로 변환
        return recordPage.map(record -> {
            CounselingSchedule schedule = record.getSchedule(); 
            
            String studentNameResult = studentRepository.findById(record.getStudentId())
                .map(Student::getName)
                .orElse("학생 정보 없음");

            CounselingScheduleResponseDto scheduleDto = new CounselingScheduleResponseDto(
                schedule,
                professorRepository.findById(professorId).map(Professor::getName).orElse("교수"),
                studentNameResult
            );

            return CounselingRecordResponseDto.fromEntity(record, scheduleDto);
        });
    }
    
    // 교수용: 확정/진행 중인 상담 목록 조회 (COMPLETED 제외)
    @Transactional(readOnly = true)
    public List<CounselingScheduleResponseDto> getConfirmedSchedulesForProfessor(Long professorId) {
        
        List<CounselingSchedule> confirmedSchedules = scheduleRepository.findByProfessorIdAndStatus(professorId, ScheduleStatus.CONFIRMED);
        
        return confirmedSchedules.stream()
                .map(schedule -> {
                    String professorName = professorRepository.findById(professorId).map(Professor::getName).orElse("교수");
                    String studentName = studentRepository.findById(schedule.getStudentId()).map(Student::getName).orElse("학생");
                    
                    return new CounselingScheduleResponseDto(
                        schedule, 
                        professorName, 
                        studentName
                    );
                })
                .toList();
    }
    
    // 특정 상담 기록 조회
    public CounselingRecord getRecordByScheduleId(Long scheduleId, Long studentId) {
        return recordRepository.findByScheduleIdAndStudentId(scheduleId, studentId)
                .orElseThrow(() -> new CustomRestfullException("기록된 상담 내용이 없습니다.", HttpStatus.NOT_FOUND));
    }
    
    // 학생용: 특정 상담 기록 조회
    @Transactional(readOnly = true)
    public CounselingRecordResponseDto getRecordForStudent(Long scheduleId, Long studentId) {

        CounselingSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomRestfullException("상담 일정이 존재하지 않습니다.", HttpStatus.NOT_FOUND));

        if (!schedule.getStudentId().equals(studentId)) {
            throw new CustomRestfullException("해당 상담 일정을 조회할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        Long professorId = schedule.getProfessorId(); 
        Professor professor = professorRepository.findById(professorId)
            .orElseThrow(() -> new CustomRestfullException("교수 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        String professorName = professor.getName(); 

        String studentName = studentService.getStudentName(studentId);

        Optional<CounselingRecord> optionalRecord = recordRepository.findByScheduleIdAndStudentId(scheduleId, studentId);

        CounselingScheduleResponseDto scheduleDto = new CounselingScheduleResponseDto(
            schedule, 
            professorName, 
            studentName
        );

        if (optionalRecord.isEmpty()) {
            return CounselingRecordResponseDto.fromEmptyRecord(scheduleDto, studentName, studentId); 
        } else {
            CounselingRecord record = optionalRecord.get();
            return CounselingRecordResponseDto.fromEntity(record, scheduleDto); 
        }
    }
    
    // 교수용: 특정 상담 기록 조회
    @Transactional(readOnly = true)
    public CounselingRecordResponseDto getRecordForProfessor(Long scheduleId, Long studentId, Long professorId) {

        CounselingSchedule schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new CustomRestfullException("상담 일정이 존재하지 않습니다.", HttpStatus.NOT_FOUND));

        if (!schedule.getProfessorId().equals(professorId)) {
            throw new CustomRestfullException("해당 상담 기록을 조회할 권한이 없습니다. (담당 교수가 아님)", HttpStatus.FORBIDDEN);
        }
        
        Professor professor = professorRepository.findById(schedule.getProfessorId())
            .orElseThrow(() -> new CustomRestfullException("교수 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        String professorName = professor.getName(); 
        
        String studentName;
        try {
            studentName = studentRepository.findById(studentId) 
                .map(Student::getName)
                .orElse("학생 정보 조회 실패");
        } catch (Exception e) {
            studentName = "학생 정보 조회 실패";
        }

        Optional<CounselingRecord> optionalRecord = recordRepository.findByScheduleIdAndStudentId(scheduleId, studentId);

        CounselingScheduleResponseDto scheduleDto = new CounselingScheduleResponseDto(
            schedule, 
            professorName, 
            studentName
        );

        if (optionalRecord.isEmpty()) {
            return CounselingRecordResponseDto.fromEmptyRecord(scheduleDto, studentName, studentId); 
        } else {
            CounselingRecord record = optionalRecord.get();
            return CounselingRecordResponseDto.fromEntity(record, scheduleDto); 
        }
    }
    
}
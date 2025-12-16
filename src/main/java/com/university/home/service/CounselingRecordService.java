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
import com.university.home.service.StudentService; 
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

// JPA Join을 위한 import 추가 (필요 시)
import jakarta.persistence.criteria.Join; 

@Service
@RequiredArgsConstructor
public class CounselingRecordService {
	// @Autowired 제거: final 필드에 @RequiredArgsConstructor로 주입 (권장 방식)
    private final CounselingRecordRepository recordRepository;
	
	private final StudentRepository studentRepository;
    private final CounselingScheduleRepository scheduleRepository;
    private final StudentService studentService; 
    
    // final이 아닌 필드에 대해서만 @Autowired 유지
    @Autowired
    private ProfessorRepository professorRepository;
    
    // [1] 상담 기록 저장 (STT 완료 또는 교수자 메모 입력 시)
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
    
    // [1-2] 완료된 상담 기록 목록 조회 (페이지네이션/검색이 없는 단순 목록)
    public List<CounselingRecordResponseDto> getProfessorRecordList(Long professorId) {
        // 1. 해당 교수의 완료된(COMPLETED) 상담 일정을 모두 조회
        List<CounselingSchedule> completedSchedules = scheduleRepository.findByProfessorIdAndStatus(professorId, ScheduleStatus.COMPLETED);
        
        return completedSchedules.stream()
                .map(schedule -> {
                    // 2. Schedule ID로 해당 Record를 조회 (Optional)
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
    
    // [2] 상담 내용 검색 (학생 이름, 상담 날짜, 키워드 등) - ⭐️ 핵심 수정 부분
    @Transactional(readOnly = true)
    public Page<CounselingRecordResponseDto> searchRecords(
        Long professorId, 
        String studentName, 
        String consultationDateStr, 
        String keyword,
        Pageable pageable 
    ) {
        
        // 1. Specification 구성
    	Specification<CounselingRecord> spec = Specification.where((root, query, builder) -> 
        // Join Type 문제 발생 가능성을 줄이기 위해, 가장 단순한 형태의 관계 접근을 다시 시도합니다.
        builder.equal(root.get("schedule").get("professorId"), professorId)
    );

        // 2. 학생 이름 검색 (기존 로직 유지)
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
        
        // 3. 날짜 검색 (기존 로직 유지)
        if (consultationDateStr != null && !consultationDateStr.trim().isEmpty()) {
            // ... (기존 날짜 검색 로직)
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
        
        // 4. 키워드/내용 검색 (기존 로직 유지)
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
            
            spec = spec.and((root, query, builder) -> 
                builder.or(
                    builder.like(builder.lower(root.get("notes")), likeKeyword),
                    builder.like(builder.lower(root.get("keywords")), likeKeyword)
                )
            );
        }

        // 5. 최종 검색 실행
        // 💡 Fetch Join을 직접 사용하지 않고, Specification을 사용하는 경우,
        //    DTO 변환 시 N+1 쿼리 방지를 위해 DTO 변환 시 scheduleRepository.findById를 사용했던 코드를 제거하고
        //    record.getSchedule()을 직접 사용합니다. (이미 OneToOne 매핑되어 있으므로)
        Page<CounselingRecord> recordPage = recordRepository.findAll(spec, pageable);
        
        // 6. DTO로 변환
        return recordPage.map(record -> {
            // 🚨 record.getSchedule()을 사용하여 지연 로딩을 트리거합니다. (N+1 문제 발생 가능하지만, 일단 목록 표시를 우선합니다)
            CounselingSchedule schedule = record.getSchedule(); 
            
            // DTO 생성에 필요한 나머지 정보 조회
            String studentNameResult = studentRepository.findById(record.getStudentId())
                .map(Student::getName)
                .orElse("학생 정보 없음");

            CounselingScheduleResponseDto scheduleDto = new CounselingScheduleResponseDto(
                schedule, // 이미 엔티티에 매핑된 schedule 객체 사용
                professorRepository.findById(professorId).map(Professor::getName).orElse("교수"),
                studentNameResult
            );

            return CounselingRecordResponseDto.fromEntity(record, scheduleDto);
        });
    }
    
    // [3] 교수용: 확정/진행 중인 상담 목록 조회 (COMPLETED 제외)
    @Transactional(readOnly = true)
    public List<CounselingScheduleResponseDto> getConfirmedSchedulesForProfessor(Long professorId) {
        
        // ScheduleStatus.CONFIRMED 상태의 일정만 조회
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
    
    // [4] 특정 상담 기록 조회 (다른 메서드에서 사용)
    public CounselingRecord getRecordByScheduleId(Long scheduleId, Long studentId) {
        return recordRepository.findByScheduleIdAndStudentId(scheduleId, studentId)
                .orElseThrow(() -> new CustomRestfullException("기록된 상담 내용이 없습니다.", HttpStatus.NOT_FOUND));
    }
    
    // [5] 학생용: 특정 상담 기록 조회
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

        // TODO: studentService.getStudentName(studentId)가 StudentService에 구현되어 있어야 함
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
    
    // [6] 교수용: 특정 상담 기록 조회
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
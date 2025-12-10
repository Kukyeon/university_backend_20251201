
package com.university.home.dto;

import com.university.home.entity.CounselingRecord;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CounselingRecordResponseDto {

    private Long id;
    private String studentName;
    private Long studentId;
    private LocalDateTime consultationDate;
    private String notes;
    private String keywords;
    private LocalDateTime recordDate;

  
    private CounselingScheduleResponseDto schedule; 
    
  
    public static CounselingRecordResponseDto fromEntity(
        CounselingRecord record, 
        CounselingScheduleResponseDto scheduleDto
    ) {
        return CounselingRecordResponseDto.builder()
            .id(record.getId())
            .studentName(record.getStudentName())
            .studentId(record.getStudentId())
            .consultationDate(record.getConsultationDate())
            .notes(record.getNotes())
            .keywords(record.getKeywords())
            .recordDate(record.getRecordDate())
            .schedule(scheduleDto) // ⭐️ Schedule DTO 주입
            .build();
    }
    public static CounselingRecordResponseDto fromEmptyRecord(
            CounselingScheduleResponseDto scheduleDto,
            String studentName,
            Long studentId
        ) {
            return CounselingRecordResponseDto.builder()
                .id(null) // Record ID 없음
                .studentName(studentName) // Schedule에서 가져온 학생 이름
                .studentId(studentId) // Schedule에서 가져온 학생 ID
                // 상담 날짜는 schedule의 시작 시간을 사용 (일정이 존재한다는 의미)
                .consultationDate(scheduleDto.getStartTime()) 
                // 기록이 없음을 명시
                .notes("아직 상담 기록이 작성되지 않았습니다. 상담이 완료되면 내용이 표시됩니다.") 
                .keywords("")
                .recordDate(null)
                .schedule(scheduleDto) // Schedule DTO는 필수 포함
                .build();
        }
    
    
}
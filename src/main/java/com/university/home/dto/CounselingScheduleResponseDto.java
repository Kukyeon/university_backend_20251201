package com.university.home.dto;

import java.time.LocalDateTime;

import com.university.home.entity.CounselingSchedule;
import com.university.home.entity.ScheduleStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CounselingScheduleResponseDto {

    private Long scheduleId;
    private String professorName;
    private String studentName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    public CounselingScheduleResponseDto(CounselingSchedule schedule, String professorName, String studentName) {
        this.scheduleId = schedule.getId();
        this.professorName = professorName;
        this.studentName = studentName; // ⭐️ 추가
        this.startTime = schedule.getStartTime();
        this.endTime = schedule.getEndTime();
        // ⭐️ [개선] 상태 Enum을 한글로 변환하는 메서드를 사용하도록 수정 (아래 B 참조)
        this.status = convertStatusToKorean(schedule.getStatus()); 
    }
    
    // ⭐️ [추가] 상태(ScheduleStatus)를 한글로 변환하는 헬퍼 메서드
    private String convertStatusToKorean(ScheduleStatus status) {
        switch (status) {
            case CONFIRMED:
                return "확인됨"; // 또는 "예약 완료"
            case CANCELED:
                return "취소됨";
            case COMPLETED:
                return "상담 완료";
            default:
                return status.name(); // 정의되지 않은 상태는 그대로 영문 사용
        }
    }
}
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
    private Long professorId;
    private String professorName;
    private String studentName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    public CounselingScheduleResponseDto(CounselingSchedule schedule, String professorName, String studentName) {
        this.scheduleId = schedule.getId();
        this.professorId = schedule.getProfessorId();
        this.professorName = professorName;
        this.studentName = studentName; // ⭐️ 추가
        this.startTime = schedule.getStartTime();
        this.endTime = schedule.getEndTime();
        this.status = convertStatusToKorean(schedule.getStatus()); 
    }
    
    private String convertStatusToKorean(ScheduleStatus status) {
        switch (status) {
            case CONFIRMED:
                return "예약 완료";
            case CANCELED:
                return "예약 취소";
            case COMPLETED:
                return "상담 완료";
            case IN_PROGRESS:
                return "상담 진행중";
            case PENDING:
                return "확인중";
            case NO_SHOW:
                return "미참석";
            default:
                return status.name(); // 정의되지 않은 상태는 그대로 영문 사용
        }
    }
}
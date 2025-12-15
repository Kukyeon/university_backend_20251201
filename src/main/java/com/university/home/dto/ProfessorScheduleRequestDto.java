package com.university.home.dto;

import java.time.LocalDateTime;
import com.university.home.entity.ScheduleStatus;
import com.university.home.entity.CounselingSchedule;
import lombok.Getter;

@Getter
public class ProfessorScheduleRequestDto {

    private Long id;
    private Long studentId;
    private String studentName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ScheduleStatus status;

    public ProfessorScheduleRequestDto(
        CounselingSchedule schedule,
        String studentName
    ) {
        this.id = schedule.getId();
        this.studentId = schedule.getStudentId();
        this.studentName = studentName;
        this.startTime = schedule.getStartTime();
        this.endTime = schedule.getEndTime();
        this.status = schedule.getStatus();
    }
}

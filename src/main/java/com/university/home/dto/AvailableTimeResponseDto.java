package com.university.home.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AvailableTimeResponseDto {

    private Long id;
    private Long professorId;
    private String professorName;
    private String department;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    public AvailableTimeResponseDto(
            Long id,
            Long professorId,
            String professorName,
            LocalDateTime startTime,
            LocalDateTime endTime
        ) {
            this.id = id;
            this.professorId = professorId;
            this.professorName = professorName;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public Long getId() { return id; }
        public Long getProfessorId() { return professorId; }
        public String getProfessorName() { return professorName; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
    }


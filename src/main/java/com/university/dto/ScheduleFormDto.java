package com.university.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ScheduleFormDto {
    private Long id;
    private Long staffId;
    private LocalDateTime startDay;
    private LocalDateTime endDay;
    private String information;
}

package com.university.home.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class ScheduleFormDto {
    private Long id;
    private Long staffId;
    private LocalDate startDay;
    private LocalDate endDay;
    private String information;
}

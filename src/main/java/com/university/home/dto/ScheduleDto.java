package com.university.home.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class ScheduleDto {
    private Long id;
    private Long staffId;
    private Integer month;
    private Integer year;
    private String startMday;
    private String endMday;
    private LocalDate startDay;
    private LocalDate endDay;
    private String information;
    private Integer sum;
}

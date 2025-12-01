package com.university.home.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ScheduleDto {
    private Long id;
    private Long staffId;
    private Integer month;
    private Integer year;
    private String startMday;
    private String endMday;
    private LocalDateTime startDay;
    private LocalDateTime endDay;
    private String information;
    private Integer sum; // 월별 일정 합계 등 필요 시
}

package com.university.home.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AvailabilityRequestDto {
	@NotNull(message = "시작 시간을 입력해주세요.")
    @FutureOrPresent(message = "시작 시간은 현재 또는 미래여야 합니다.")
    private LocalDateTime startTime;
	
	@NotNull(message = "종료 시간을 입력해주세요.")
    @Future(message = "종료 시간은 현재보다 미래여야 합니다.")
    private LocalDateTime endTime;
}
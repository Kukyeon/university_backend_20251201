package com.university.home.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequestDto {
	@NotNull(message = "예약하려는 가능 시간 ID를 지정해야 합니다.")
    private Long availabilityId; // 예약하려는 가능 시간 블록 ID
	
	@NotNull(message = "학생 ID는 필수입니다.")
    private Long studentId;      // 예약하는 학생 ID
}

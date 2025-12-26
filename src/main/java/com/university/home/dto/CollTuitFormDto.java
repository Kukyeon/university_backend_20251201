package com.university.home.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CollTuitFormDto {

	@NotNull(message = "단과대 ID는 필수입니다.")
    private Long collegeId;
	
	private String collegeName;

    @NotNull(message = "등록금 금액은 필수입니다.")
    private Long amount;

    public String amountFormat() {
        if (amount == null) return "";
        return String.format("%,d", amount); // 1000단위 콤마
    }
}

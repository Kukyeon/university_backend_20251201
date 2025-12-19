package com.university.home.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EntryValidateDto {

	private boolean canEnter;
    private String reason;

    public static EntryValidateDto ok() {
        return new EntryValidateDto(true, "");
    }

    public static EntryValidateDto fail(String reason) {
        return new EntryValidateDto(false, reason);
    }
}

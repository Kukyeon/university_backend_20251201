package com.university.home.dto;


import com.sun.istack.NotNull;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubjectDto {

	private Long id;

    @NotEmpty
    @Size(min = 2, max = 20)
    private String name;

    @NotNull
    @Min(10000000)
    @Max(99999999)
    private Long professorId;

    @Size(max = 5)
    private String roomId;

    @NotNull
    private Long deptId;

    @NotEmpty
    @Size(max = 2)
    private String type;

    @NotNull
    private Long subYear;

    @NotNull
    @Min(1)
    @Max(2)
    private Long semester;

    @NotEmpty
    @Size(max = 1)
    private String subDay;

    @NotNull
    @Min(9)
    @Max(18)
    private Long startTime;

    @NotNull
    @Min(9)
    @Max(18)
    private Long endTime;

    @NotNull
    private Long grades;

    @NotNull
    private Long capacity;

    private Long numOfStudent;
}

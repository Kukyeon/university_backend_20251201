package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "schedule_tb")
@Getter @Setter
@NoArgsConstructor
public class Schedule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Integer -> Long

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @Column(name = "start_day")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDay;

    @Column(name = "end_day")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDay;

    private String information;
}
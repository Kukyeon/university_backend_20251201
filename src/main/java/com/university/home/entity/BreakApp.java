package com.university.home.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "break_app_tb")
@Getter
@Setter
@NoArgsConstructor
public class BreakApp { // 휴학
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "student_grade")
    private Long studentGrade;

    @Column(name = "from_year")
    private Long fromYear; 
    @Column(name = "from_semester")
    private Long fromSemester; 
    @Column(name = "to_year")
    private Long toYear; 
    @Column(name = "to_semester")
    private Long toSemester; 

    private String type;
    private LocalDate appDate;
    private String status ="처리중";
}
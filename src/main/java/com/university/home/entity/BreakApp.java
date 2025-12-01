package com.university.home.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "break_app_tb")
@Getter
@Setter
@NoArgsConstructor
public class BreakApp {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Integer -> Long

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "student_grade")
    private Long studentGrade; // Integer -> Long

    @Column(name = "from_year")
    private Long fromYear; // Integer -> Long
    @Column(name = "from_semester")
    private Long fromSemester; // Integer -> Long
    @Column(name = "to_year")
    private Long toYear; // Integer -> Long
    @Column(name = "to_semester")
    private Long toSemester; // Integer -> Long

    private String type;
    private String status;
}
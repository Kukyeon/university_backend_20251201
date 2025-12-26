package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tuition_tb")
@Getter @Setter
@NoArgsConstructor
public class Tuition { // 등록금
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "tui_year")
    private Long tuiYear;

    private Long semester;

    @Column(name = "tui_amount")
    private Long tuiAmount;

    @ManyToOne
    @JoinColumn(name = "sch_type")
    private Scholarship scholarshipType;

    @Column(name = "sch_amount")
    private Long schAmount;

    @Column(nullable = false)
    private Boolean status = false; 
}
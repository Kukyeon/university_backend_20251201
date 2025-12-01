package com.university.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tuition_tb")
@Getter @Setter
@NoArgsConstructor
public class Tuition {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Integer -> Long

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "tui_year")
    private Long tuiYear; // Integer -> Long

    private Long semester; // Integer -> Long

    @Column(name = "tui_amount")
    private Long tuiAmount; // Integer -> Long

    @Column(name = "sch_type")
    private Long schType; // Integer -> Long

    @Column(name = "sch_amount")
    private Long schAmount; // Integer -> Long

    private Boolean status; 
}
package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stu_sch_tb")
@Getter @Setter
@NoArgsConstructor
public class StuSch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Integer -> Long

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "sch_year")
    private Long schYear; // Integer -> Long
    private Long semester; // Integer -> Long
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sch_type")
    private Scholarship scholarshipType;
}
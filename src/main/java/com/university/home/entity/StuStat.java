package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "stu_stat_tb")
@Getter @Setter
@NoArgsConstructor
public class StuStat { // 학생 학적상태
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    private String status;
    
    @Column(name = "from_date")
    private LocalDate fromDate;
    
    @Column(name = "to_date")
    private LocalDate toDate;

    @Column(name = "break_app_id")
    private Long breakAppId; 
}
package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stu_sub_detail_tb")
@Getter @Setter
@NoArgsConstructor
public class StuSubDetail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Integer -> Long

    @Column(name = "student_id")
    private Long studentId; // Integer -> Long

    @Column(name = "subject_id")
    private Long subjectId; // Integer -> Long

    private Long absent; // Integer -> Long
    private Long lateness; 
    private Long homework;
    private Long midExam;
    private Long finalExam;
    private Long convertedMark; 
}
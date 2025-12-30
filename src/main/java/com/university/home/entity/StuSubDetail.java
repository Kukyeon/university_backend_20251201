package com.university.home.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stu_sub_detail_tb")
@Getter @Setter
@NoArgsConstructor
public class StuSubDetail { // 교수용 학생 성적기입
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // StuSub.id를 그대로 사용
    @JsonIgnore
    @JoinColumn(name = "id")
    private StuSub stuSub;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;
    
    private Long absent; 
    private Long lateness; 
    private Long homework;
    private Long midExam;
    private Long finalExam;
    private Long convertedMark; 
}
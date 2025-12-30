package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "evaluation_tb")
@Getter @Setter
@NoArgsConstructor
public class Evaluation { // 강의평가 답변지
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evaluation_id")
    private Long id;

    private Long answer1;
    private Long answer2;
    private Long answer3;
    private Long answer4;
    private Long answer5;
    private Long answer6;
    private Long answer7;
    private String improvements;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stu_sub_id")
    private StuSub stuSub;

}

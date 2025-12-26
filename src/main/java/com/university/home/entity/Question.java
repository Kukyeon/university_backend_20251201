package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "question_tb")
@Getter @Setter
@NoArgsConstructor
public class Question { // 강의평가 질문
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(name = "question1")
    private String question1;

    @Column(name = "question2")
    private String question2;

    @Column(name = "question3")
    private String question3;
    
    @Column(name = "question4")
    private String question4;

    @Column(name = "question5")
    private String question5;

    @Column(name = "question6")
    private String question6;

    @Column(name = "question7")
    private String question7;

    private String title;
    private String content;
    private Long userId;
    
    @Column(name = "sug_content")
    private String sugContent;
}
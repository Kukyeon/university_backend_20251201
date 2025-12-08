package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "question_tb")
@Getter @Setter
@NoArgsConstructor
public class Question {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Integer -> Long

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

    // DB 스키마에 존재하는 다른 필드들도 추가합니다. (사용하지 않더라도 매핑 필요)
    private String title;
    private String content; // 이전 코드가 사용하던 content 필드 (DB에 존재)
    private Long userId;
    
    @Column(name = "sug_content")
    private String sugContent; // DB 스키마에 sug_content가 있으므로 추가
}
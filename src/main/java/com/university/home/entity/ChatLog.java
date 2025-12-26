package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
@Table(name = "chat_log_tb")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatLog { // 챗봇

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    @JsonIgnore
    private Student student;

    @Column(columnDefinition = "TEXT")
    private String question; // 학생 질문

    @Column(columnDefinition = "TEXT")
    private String answer;   // AI 답변

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

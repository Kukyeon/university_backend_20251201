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
public class ChatLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 질문한 학생 (로그인 안 해도 되면 Nullable, 여기선 로그인 가정)
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

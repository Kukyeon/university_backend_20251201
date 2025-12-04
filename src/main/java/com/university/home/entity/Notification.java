package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_tb")
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long receiverId; // 알림 받을 사람 (교수 ID)
    private String content;  // 알림 내용 ("OOO 학생이 위험 단계입니다.")
    private String url;      // 클릭 시 이동할 링크 (예: "/professor/dashboard")
    
    @Column(name = "is_read")
    private boolean isRead;  // 읽음 여부 (false: 안 읽음)
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
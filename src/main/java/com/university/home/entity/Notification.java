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
    
    private Long senderId; // 알림 보낸 사람 (교수ID)
    private String type; // 알림 타입 ( AI 랑 교수 )
    
    private String content;  // 알림 내용 ("OOO 학생이 위험 단계입니다.")
    private String url;      // 클릭 시 이동할 링크 (예: "/professor/dashboard")
    
    @Column(name = "is_read")
    private boolean isRead;  // 읽음 여부 (false: 안 읽음)
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
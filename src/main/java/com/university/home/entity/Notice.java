package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notice_tb")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice { // 공지사항

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Column(name = "content", length = 2000)
    private String content;

    @Builder.Default
    private Long views = 0L;  

    private String category;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    private String imageUrl;

    @PrePersist
    public void prePersist() {
        if (createdTime == null) createdTime = LocalDateTime.now();
        if (views == null) views = 0L;
    }
}

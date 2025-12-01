package com.university.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notice_tb")
@Getter @Setter
@NoArgsConstructor
public class Notice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Integer -> Long

    private String title;
    private String content;
    private Long views; // Integer -> Long
    private String category;
    
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL)
    private List<NoticeFile> files = new ArrayList<>();
}
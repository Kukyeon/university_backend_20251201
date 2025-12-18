package com.university.home.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "counseling_record_tb")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CounselingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private CounselingSchedule schedule;

    // 검색 용이성을 위해 User 정보를 중복 저장
    private String studentName; 
    private Long studentId;
    
    @Column(nullable = false)
    private LocalDateTime consultationDate; 
    
    @Lob 
    private String notes; // STT 전사 내용 또는 교수자 메모
    
    private String keywords; // 검색용 키워드
    
    private LocalDateTime recordDate = LocalDateTime.now();
    
}
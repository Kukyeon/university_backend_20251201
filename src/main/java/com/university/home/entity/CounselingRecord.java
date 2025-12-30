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
public class CounselingRecord { // 화상상담기록
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private CounselingSchedule schedule;

    private String studentName; 
    private Long studentId;
    
    @Column(nullable = false)
    private LocalDateTime consultationDate; 
    
    @Lob 
    private String notes; // STT 전사 내용 또는 교수자 메모
    
    private String keywords; // 검색용 키워드
    
    private LocalDateTime recordDate = LocalDateTime.now();
    
    private LocalDateTime startedAt;  // 화상 상담 입장 시간
    private LocalDateTime finishedAt; // 상담 종료 시간
    @Enumerated(EnumType.STRING)
    private ScheduleStatus status;    // PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELED, NO_SHOW

    
}
package com.university.home.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "counseling_schedule_tb")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CounselingSchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleStatus status = ScheduleStatus.CONFIRMED;

    // User.id를 외래 키로 사용
    @Column(nullable = false)
    private Long professorId;
    @Column(nullable = false)
    private Long studentId; 
    
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "availability_id", nullable = false)
    private ProfessorAvailability availability;
    
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    @Column(nullable = false)
    private LocalDateTime endTime;
}
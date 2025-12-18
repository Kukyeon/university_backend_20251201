package com.university.home.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "professor_availability_tb")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorAvailability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User.id를 외래 키로 사용 (교수 ID)
    @Column(nullable = false)
    private Long professorId; 
    
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    @Column(nullable = false)
    private LocalDateTime endTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityStatus status = AvailabilityStatus.OPEN;

    @Column(nullable = false)
    private boolean active = true;  // 예약 가능 여부
    
    @Column(name = "is_booked", nullable = false)
    private boolean isBooked = false;
}
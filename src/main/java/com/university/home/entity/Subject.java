package com.university.home.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subject_tb")
@Getter @Setter
@NoArgsConstructor
public class Subject {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Integer -> Long
    
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id")
    private Professor professor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private Department department;

    private String type; // 전공선택, 교양 등
    private Long subYear; // Integer -> Long
    private Long semester; // Integer -> Long
    private String subDay;
    private Long startTime; // Integer -> Long
    private Long endTime; // Integer -> Long
    private Long grades; // Integer -> Long (학점)
    private Long capacity; // Integer -> Long (정원)
    private Long numOfStudent = 0L; // Integer -> Long (신청인원)
    @Column(columnDefinition = "integer default 0")
    private Integer basketCount = 0; //예비 수강신청인원
    @OneToOne(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Syllabus syllabus;
}

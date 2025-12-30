package com.university.home.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "syllabus_tb")
@Getter @Setter
@NoArgsConstructor
public class Syllabus { // 강의계획서
    
    @Id
    @Column(name = "subject_id")
    private Long subjectId; 

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId 
    @JsonIgnore
    @JoinColumn(name = "subject_id")
    private Subject subject;

    private String overview;
    private String objective;
    private String textbook;
    private String program;
}
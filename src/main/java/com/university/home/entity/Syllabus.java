package com.university.home.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "syllabus_tb")
@Getter @Setter
@NoArgsConstructor
public class Syllabus {
    
    @Id
    @Column(name = "subject_id")
    private Long subjectId; // Integer -> Long

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
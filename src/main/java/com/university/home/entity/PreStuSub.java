package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Table(name = "pre_stu_sub_tb")
@Getter @Setter
@NoArgsConstructor
public class PreStuSub { // 수강내역
    
    @EmbeddedId
    private PreStuSubId id = new PreStuSubId(); // 초기화 필요

    @MapsId("studentId") 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @MapsId("subjectId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Embeddable
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class PreStuSubId implements Serializable {
        @Column(name = "student_id")
        private Long studentId; 
        @Column(name = "subject_id")
        private Long subjectId; 
    }
}
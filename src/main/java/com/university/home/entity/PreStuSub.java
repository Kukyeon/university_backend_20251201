package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Table(name = "pre_stu_sub_tb")
@Getter @Setter
@NoArgsConstructor
public class PreStuSub {
    
    @EmbeddedId
    private PreStuSubId id = new PreStuSubId(); // 초기화 필요

    // ★ 추가됨: 복합키의 studentId와 매핑되는 실제 Student 객체
    @MapsId("studentId") 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    // ★ 추가됨: 복합키의 subjectId와 매핑되는 실제 Subject 객체
    @MapsId("subjectId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    // 복합키 클래스 (기존 유지)
    @Embeddable
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class PreStuSubId implements Serializable {
        @Column(name = "student_id")
        private Long studentId; 
        @Column(name = "subject_id")
        private Long subjectId; 
    }
}
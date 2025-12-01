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
    private PreStuSubId id;

    @Embeddable
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class PreStuSubId implements Serializable {
        @Column(name = "student_id")
        private Long studentId; // Integer -> Long
        @Column(name = "subject_id")
        private Long subjectId; // Integer -> Long
    }
}

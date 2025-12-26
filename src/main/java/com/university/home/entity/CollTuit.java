package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "coll_tuit_tb")
@Getter @Setter
@NoArgsConstructor
public class CollTuit { // 단과대 등록금
    @Id
    @Column(name = "college_id")
    private Long collegeId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "college_id")
    private College college;

    private Long amount;
}
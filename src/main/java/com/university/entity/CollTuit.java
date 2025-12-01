package com.university.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "coll_tuit_tb")
@Getter @Setter
@NoArgsConstructor
public class CollTuit {
    @Id
    @Column(name = "college_id")
    private Long collegeId; // Integer -> Long

    @OneToOne
    @MapsId
    @JoinColumn(name = "college_id")
    private College college;

    private Long amount; // Integer -> Long
}
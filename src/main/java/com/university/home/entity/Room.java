package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "room_tb")
@Getter @Setter @NoArgsConstructor
public class Room { // 강의실
    @Id 
    private String id; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id")
    private College college;
}

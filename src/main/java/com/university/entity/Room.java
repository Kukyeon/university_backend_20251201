package com.university.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "room_tb")
@Getter @NoArgsConstructor
public class Room {
    @Id 
    private String id; // 강의실 호수는 보통 문자열(예: E101)이 많아 유지했으나, 숫자만 쓴다면 Long 변경 가능

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id")
    private College college;
}

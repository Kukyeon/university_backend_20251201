package com.university.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "college_tb")
@Getter @NoArgsConstructor
public class College {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Integer -> Long
    private String name;
}

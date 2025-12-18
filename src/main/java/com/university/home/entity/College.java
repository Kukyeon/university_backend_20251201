package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "college_tb")
@Getter @NoArgsConstructor
@Setter
public class College {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Integer -> Long
    private String name;
}

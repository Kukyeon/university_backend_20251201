package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "scholarship_tb")
@Getter @NoArgsConstructor
public class Scholarship {
    @Id
    private Long type; // Integer -> Long

    @Column(name = "max_amount")
    private Long maxAmount; // Integer -> Long
}
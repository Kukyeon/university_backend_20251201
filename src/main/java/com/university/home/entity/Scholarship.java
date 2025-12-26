package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "scholarship_tb")
@Getter @NoArgsConstructor
public class Scholarship { // 장학금 유형
    @Id
    private Long type;

    @Column(name = "max_amount")
    private Long maxAmount;
}
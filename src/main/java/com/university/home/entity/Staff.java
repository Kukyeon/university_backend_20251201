package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "staff_tb")
@Getter @Setter
@NoArgsConstructor
public class Staff {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Integer -> Long

    private String name;
    private LocalDate birthDate;
    private String gender;
    private String address;
    private String tel;
    private String email;
}
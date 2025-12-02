package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "student_tb")
@Getter @Setter
@NoArgsConstructor
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Integer -> Long
    
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user; // User와 1:1 매핑
    
    private String name;
    private LocalDate birthDate;
    private String gender;
    private String address;
    private String tel;
    private String email;
    
    private Long grade; // Integer -> Long (학년)
    private Long semester; // Integer -> Long (학기)
    private LocalDate entranceDate;
    private LocalDate graduationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private Department department;
}
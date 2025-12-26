package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "professor_tb")
@Getter @Setter
@NoArgsConstructor
public class Professor { // 교수
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 
    
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private String name;
    private LocalDate birthDate;
    private String gender;
    private String address;
    private String tel;
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private Department department;
    
    private LocalDate hireDate;
    
   
}

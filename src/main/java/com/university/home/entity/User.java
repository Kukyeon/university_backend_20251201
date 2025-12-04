package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_tb")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id 
    private Long id; // Integer -> Long

    @Column(nullable = false)
    private String password;

    @Column(name = "user_role", nullable = false)
    private String userRole; 
}

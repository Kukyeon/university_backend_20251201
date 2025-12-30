package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_tb")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User { // 로그인/권한용 유저
    
    @Id 
    private Long id;

    @Column(nullable = false)
    private String password;

    @Column(name = "user_role", nullable = false)
    private String userRole; 
}

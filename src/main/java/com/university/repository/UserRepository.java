package com.university.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.entity.User;

public interface UserRepository extends JpaRepository<User, Long>{

}

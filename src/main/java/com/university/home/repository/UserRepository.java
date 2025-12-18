package com.university.home.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.User;


public interface UserRepository extends JpaRepository<User, Long>{
	
	List<User> findByUserRole(String userRole);

}

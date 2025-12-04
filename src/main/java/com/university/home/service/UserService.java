package com.university.home.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.university.home.entity.User;
import com.university.home.exception.CustomRestfullException;
import com.university.home.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {
	
	@Autowired
	UserRepository userRepository;
	@Autowired
	PasswordEncoder encoder;
	
	@Transactional
	public User createUser(Long id, String role) {
		User user = new User();
		user.setId(id);
		user.setUserRole(role);
		user.setPassword(encoder.encode(id.toString()));
		
		return userRepository.save(user);
	}
	@Transactional
	public User login(Long id, String rawPassword) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new CustomRestfullException("User not found", HttpStatus.NOT_FOUND));
		if (!encoder.matches(rawPassword, user.getPassword())) {
			throw new CustomRestfullException("Invalid password", HttpStatus.NOT_FOUND);
		}
		return user;
	}
	 @Transactional
	    public void resetPassword(Long id, String tempPassword) {
	        User user = userRepository.findById(id)
	                .orElseThrow(() -> new CustomRestfullException("User not found", HttpStatus.NOT_FOUND));
	        user.setPassword(encoder.encode(tempPassword));
	    }
	
}

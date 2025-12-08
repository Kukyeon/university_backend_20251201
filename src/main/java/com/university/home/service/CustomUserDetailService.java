package com.university.home.service;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.university.home.entity.User;
import com.university.home.repository.UserRepository;

@Service
public class CustomUserDetailService implements UserDetailsService {

	private final UserRepository userRepository;
	
	 public CustomUserDetailService(UserRepository userRepository) {
	        this.userRepository = userRepository;
	    }
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findById(Long.valueOf(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

		List<SimpleGrantedAuthority> authorities = List.of(
	            new SimpleGrantedAuthority("ROLE_" + user.getUserRole()) 
		    );
		 return new CustomUserDetails(user);
	}

	
}

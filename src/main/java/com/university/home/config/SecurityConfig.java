package com.university.home.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//            // 1. CSRF 보안 끄기 (Postman 테스트를 위해 필수)
//            .csrf(AbstractHttpConfigurer::disable)
//            
//            // 2. 특정 주소는 로그인 없이 허용
//            .authorizeHttpRequests(auth -> auth
//                .requestMatchers("/test/**", "/api/chatbot/**", "/h2-console/**").permitAll() // 이 주소들은 프리패스!
//                .anyRequest().authenticated() // 나머지는 로그인해야 접근 가능
//            );
//            
//        return http.build();
//    }
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	    http.csrf(csrf -> csrf.disable()) // CSRF 비활성화
	        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // 모든 요청 허용
	    return http.build();
	}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

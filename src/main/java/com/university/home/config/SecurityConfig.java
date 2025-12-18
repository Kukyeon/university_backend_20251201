package com.university.home.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.university.home.service.CustomUserDetailService;
import com.university.home.utils.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailService customUserDetailService;
    private final JwtAuthenticationFilter jwtFilter;

    // application.yml에서 허용할 도메인 리스트를 가져옴
    @Value("${spring.web.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 아래 설정된 CORS 적용
            .csrf(csrf -> csrf.disable()) // JWT 사용하므로 CSRF 끔
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 안 씀 (StateLess)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/user/login", 
                        "/api/user/findId", 
                        "/api/user/findPw",
                        "/api/test",
                        "/api/notice/**",
                        "/api/notice/list*",
                        "/images/**","/api/**",
                        "/api/notification/**" ,
                        "/ws/signaling/**" ,
                         "/api/schedules/**",
                         "/api/schedules/available/professor/*",
                        "/api/notice/**",
                        "/api/notice/list*",
                        "/images/**",
                        "/api/notification/**",
                        "/ws/signaling/**"  // 공지목록 조회

                        ).permitAll() // 로그인, ID/PW 찾기 허용
                .requestMatchers(
                    "/api/user/login", 
                    "/api/user/findId", 
                    "/api/user/findPw",
                    "/api/user/check_nickname", // 닉네임 중복 확인 등 추가 가능
                    "/uploads/**",              // 파일 업로드 경로
                    "/images/**",               // 이미지 경로
                    "/ws/signaling/**",         // 웹소켓 경로
                    "/api/notice/list*",        // 공지 목록
                    "/api/notice/**"            // 공지 상세 (조회만 허용할거면 GET 메서드 제한 필요)
                ).permitAll()

                // 2. 교수 전용 기능 (Role: PROFESSOR)
                .requestMatchers(
                    "/api/schedules/professor/**",
                    "/api/schedules/availability/**",
                    "/api/schedules/requests",
                    "/api/prof/**"
                ).hasRole("PROFESSOR")

                // 3. 학생/교수 공통 인증 필요 기능 (예약, 취소 등)
                .requestMatchers(
                    "/api/schedules/book",
                    "/api/schedules/student",
                    "/api/schedules/cancel/*",
                    "/api/notification/**" 
                ).authenticated()

                // 4. 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )
            .userDetailsService(customUserDetailService)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    // 인증 실패 시 401 에러 명확하게 리턴
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"로그인이 필요합니다.\"}");
                })
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // yml에서 가져온 도메인들을 리스트로 변환하여 적용
        // (빈 문자열이 들어올 경우를 대비해 예외처리나 로그를 찍는 것도 좋음)
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        } else {
            // 실수로 설정을 안 했을 때 개발용 기본값
            config.addAllowedOrigin("http://localhost:3000");
        }

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://127.0.0.1:3000");
        config.addAllowedOrigin("http://university-frontend-bucket.s3-website.ap-northeast-2.amazonaws.com");
        config.addAllowedOrigin("https://d207tkakfktjyb.cloudfront.net");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Disposition"); // 파일 다운로드 시 필요

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
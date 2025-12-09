package com.university.home.utils;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.university.home.dto.PrincipalDto;
import com.university.home.service.CustomUserDetailService;
import com.university.home.service.CustomUserDetails;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	 private final JwtUtil jwtUtil;
	 private final CustomUserDetailService userDetailsService;

	    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailService userDetailsService) {
	        this.jwtUtil = jwtUtil;
	        this.userDetailsService = userDetailsService;
	    }
	
	    @Override
	    protected void doFilterInternal(HttpServletRequest request,
	                                    HttpServletResponse response,
	                                    FilterChain filterChain)
	            throws ServletException, IOException {

	        String header = request.getHeader("Authorization");
	        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
	            String token = header.substring(7);
	            try {
	                Claims claims = jwtUtil.extractClaims(token);
	                String userId = claims.getSubject();

	                // 1. UserDetails 가져오기 (기존 로직 유지)
	                CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(userId);
	                
	                // 2. UserDetails 기반으로 Authentication 객체 생성
	                UsernamePasswordAuthenticationToken auth =
	                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	                
	                // 3. PrincipalDto 생성 (다른 곳에서 필요하므로 유지)
	                PrincipalDto principalDto = new PrincipalDto();
	                principalDto.setId(Long.valueOf(userId)); 
	                principalDto.setUserRole(userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")); 

	                // 4. PrincipalDto를 사용한 Authentication 객체 생성 (사용되지 않으므로 사실상 제거해도 무방하지만, 일단 로직 유지)
	                UsernamePasswordAuthenticationToken auths =
	                    new UsernamePasswordAuthenticationToken(principalDto, null, userDetails.getAuthorities()); 

	                // 5. Authentication 객체에 WebDetails 설정 (선택 사항)
	                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
	                
	                // 6. SecurityContext에 저장: 
                    // 💡 중복 저장을 제거하고, 다른 팀원이 사용하는 UserDetails 기반의 'auth' 객체만 최종 저장합니다.
	                SecurityContextHolder.getContext().setAuthentication(auth);
	                
	            } catch (Exception e) {
	                // 토큰이 유효하지 않으면 SecurityContext 비워둠
	                System.err.println("JWT 인증 실패: " + e.getMessage()); // 디버깅용
	                SecurityContextHolder.clearContext();
	            }
	        }
	
	    filterChain.doFilter(request, response);
	}
}

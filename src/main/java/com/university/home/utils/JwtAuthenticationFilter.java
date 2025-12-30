package com.university.home.utils;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

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

	                CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(userId);
	                UsernamePasswordAuthenticationToken auth =
	                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	                SecurityContextHolder.getContext().setAuthentication(auth);
	            } catch (Exception e) {
	                SecurityContextHolder.clearContext();
	            }
	        }
	
	    filterChain.doFilter(request, response);
	}
}
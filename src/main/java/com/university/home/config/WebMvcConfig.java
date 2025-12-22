package com.university.home.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	//private final String uploadPath = "D:/university_backend_20251201/upload/images/";
	private final String uploadPath = "/app/upload/";
	
    @Value("${spring.web.cors.allowed-origins}")
    private String allowedOrigins;

//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//        		.allowedOrigins("http://localhost:3000")
//        		.allowedOrigins("http://172.30.1.55:3000")
//        		.allowedOrigins("http://172.30.1.55:8888")// 프론트 origin
//                .allowedOrigins(allowedOrigins.split(","))
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                .allowedHeaders("Authorization", "Content-Type", "Accept")
//                .exposedHeaders("Content-Type")
//                .allowCredentials(true);
//    }
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/images/**")
//                .addResourceLocations("file:D:/university_backend_20251201/upload/");
//    }
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 기존 로컬 테스트용
                .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000")
                // 2. S3 정적 웹사이트 호스팅 주소를 정확히 추가
                .allowedOrigins("https://university-front.s3-website.ap-northeast-2.amazonaws.com")
                // .env 설정값 반영
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*") // 모든 헤더 허용 (CORS 트러블슈팅 시 가장 확실함)
                .allowCredentials(true)
                .maxAge(3600);
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 3. 우분투 환경의 파일 시스템 경로 지정
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadPath);
    }
}

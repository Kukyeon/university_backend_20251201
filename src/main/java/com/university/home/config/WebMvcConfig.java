package com.university.home.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // application.yml의 upload.path 값을 가져옴 (예: /home/ubuntu/app/uploads)
    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 경로가 '/'로 끝나지 않으면 붙여줌 (안전장치)
        if (!uploadPath.endsWith("/")) {
            uploadPath += "/";
        }

        // 'file:' 접두어는 로컬 디스크의 경로를 의미함
        // 리눅스/윈도우 모두 호환되도록 구성
        String resourceLocation = "file:" + uploadPath;

        // 웹 브라우저에서 /uploads/** 로 접근하면 -> 실제 디스크의 uploadPath 폴더를 보여줌
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);
                
        // (기존 코드에 있던 /images/** 도 같은 곳을 바라보게 하거나, 별도 폴더를 지정 가능)
        registry.addResourceHandler("/images/**")
                .addResourceLocations(resourceLocation); 
    }
}
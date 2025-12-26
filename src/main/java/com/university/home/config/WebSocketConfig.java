package com.university.home.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.university.home.handler.SignalingHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final SignalingHandler signalingHandler;

    // SecurityConfig와 똑같은 도메인 목록을 가져옴
    @Value("${spring.web.cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // yml에 적힌 도메인들을 콤마(,)로 잘라서 배열로 만듦
        String[] origins = allowedOrigins.split(",");

        registry.addHandler(signalingHandler, "/ws/signaling/{scheduleId}")
                .setAllowedOrigins(origins); // 여기에 배열을 넣어주면 해당 도메인들만 접속 허용
    }
}
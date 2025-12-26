
package com.university.home.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SignalingHandler extends TextWebSocketHandler {

    private final Map<String, Map<String, WebSocketSession>> rooms = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    	try { 
            // URI에서 scheduleId(Room ID) 추출
            String path = session.getUri().getPath();
            String scheduleId = path.substring(path.lastIndexOf('/') + 1);
            
            // 간단한 사용자 ID 생성 (예시: Session ID 사용)
            String userId = session.getId(); 

            // 방에 세션 추가
            rooms.computeIfAbsent(scheduleId, k -> new ConcurrentHashMap<>()).put(userId, session);
            
        } catch (Exception e) {
            // 예외 발생 시 상세 로그 출력
            e.printStackTrace();
            // 오류 발생 시 연결 강제 종료
            session.close(CloseStatus.SERVER_ERROR); 
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // URI에서 scheduleId(Room ID) 추출
        String path = session.getUri().getPath();
        String scheduleId = path.substring(path.lastIndexOf('/') + 1);
        String senderId = session.getId();
        
        // 같은 방에 있는 모든 사용자에게 메시지 전송 (Offer, Answer, ICE Candidate 중계)
        Map<String, WebSocketSession> room = rooms.get(scheduleId);
        if (room != null) {
            for (Map.Entry<String, WebSocketSession> entry : room.entrySet()) {
                // 발신자 제외하고 전송
                if (!entry.getKey().equals(senderId) && entry.getValue().isOpen()) {
                    entry.getValue().sendMessage(message);
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String path = session.getUri().getPath();
        String scheduleId = path.substring(path.lastIndexOf('/') + 1);
        String userId = session.getId();

        // 방에서 세션 제거
        Map<String, WebSocketSession> room = rooms.get(scheduleId);
        if (room != null) {
            room.remove(userId);
            if (room.isEmpty()) {
                rooms.remove(scheduleId); // 방에 아무도 없으면 방 제거
            }
        }
    }
}
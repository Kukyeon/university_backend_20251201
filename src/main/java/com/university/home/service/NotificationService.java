package com.university.home.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.university.home.entity.CounselingSchedule;
import com.university.home.entity.Notification;
import com.university.home.entity.Student;
import com.university.home.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
	
	private final NotificationRepository notificationRepository;
	private static final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
	
	// 3. 상담 예약 알림 (구현 완료)
    // 이 메서드를 호출하면 교수님에게 실시간 알림이 갑니다.
    public void sendAppointmentAlert(CounselingSchedule schedule, String type) {
        // 교수님 ID 추출
        Long professorId = schedule.getProfessorId();
        Long studentId = schedule.getStudentId();
        
        String message = String.format("📅 [%s] %s 학생이 상담을 예약했습니다. (%s)", 
                type, studentId, schedule.getStartTime().toString());
        
        // 위에서 만든 send 메서드 재사용
       // send(professorId, message, "/professor/counseling"); // 교수님 상담 페이지 URL
        send(professorId, message, "/professor/counseling"); // 교수님 상담 페이지 URL
        
        System.out.println("🔔 [Notification] Sent to Prof " + professorId + ": " + message);
    }
    
 // 1. [신규] 클라이언트가 구독(연결) 요청 시 호출
    public SseEmitter subscribe(Long userId) {
        // 타임아웃 설정 (기본 60초 -> 60분으로 늘림, 끊기면 재연결함)
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);
        emitters.put(userId, emitter);

        // 만료되거나 에러 나면 저장소에서 제거
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError((e) -> emitters.remove(userId));

        // 503 에러 방지를 위한 더미 데이터 전송
        try {
            emitter.send(SseEmitter.event().name("connect").data("connected!"));
        } catch (IOException e) {
            emitters.remove(userId);
        }

        return emitter;
    }

    // 2. [수정] 알림 생성 및 실시간 전송
    @Transactional
    public void send(Long receiverId, String content, String url) { // 기존 sendAlert 등에서 호출
        // (1) DB 저장 (기존 로직)
        Notification notification = Notification.builder()
                .receiverId(receiverId)
                .content(content)
                .url(url)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);

        // (2) [신규] 실시간 전송 (접속 중이라면)
        SseEmitter emitter = emitters.get(receiverId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification") // 이벤트 이름
                        .data(notification)); // 데이터 전송
            } catch (IOException e) {
                emitters.remove(receiverId);
            }
        }
    }
    
 // 1. 내 알림 목록 조회
    @Transactional(readOnly = true)
    public List<Notification> getMyNotifications(Long userId) {
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(userId);
    }

    // 2. 알림 읽음 처리
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));
        
        // 읽음 상태 변경 (JPA의 변경 감지 기능으로 인해 save 없이도 DB 업데이트됨)
        notification.setRead(true);
    }
    
 // [추가] 알림 삭제
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        // 1. 알림 조회
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));

        // 2. 권한 확인 (본인의 알림인지 체크)
        if (!notification.getReceiverId().equals(userId)) {
            throw new IllegalStateException("본인의 알림만 삭제할 수 있습니다.");
        }

        // 3. 삭제
        notificationRepository.delete(notification);
    }
    
    
    
}
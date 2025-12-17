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
import com.university.home.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 로그 확인용 (선택)

@Slf4j // 로그 사용 시 필요
@Service
@RequiredArgsConstructor
public class NotificationService {
	
	private final NotificationRepository notificationRepository;
	
	// 메모리 누수 방지를 위해 ConcurrentHashMap 사용
	private static final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
	
	
	// 3. 상담 예약 알림
    public void sendAppointmentAlert(CounselingSchedule schedule, String type) {
        Long professorId = schedule.getProfessorId();
        Long studentId = schedule.getStudentId();
        
        String message = String.format("📅 [%s] %s 학생이 상담을 예약했습니다. (%s)", 
                type, studentId, schedule.getStartTime().toString());
        
        send(professorId, message, "/professor/counseling"); 
        
        System.out.println("🔔 [Notification] Sent to Prof " + professorId + ": " + message);
    }
    
    // 1. 클라이언트가 구독(연결) 요청 시 호출
    public SseEmitter subscribe(Long userId) {
        // 타임아웃 1시간 설정
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);
        emitters.put(userId, emitter);

        // 연결 종료/타임아웃/에러 시 맵에서 제거
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

    // 2. [핵심 수정] 알림 생성 및 실시간 전송
    @Transactional
    public void send(Long receiverId, String content, String url) { 
        // (1) DB 저장
        Notification notification = Notification.builder()
                .receiverId(receiverId)
                .content(content)
                .url(url)
                .Checked(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);

        // (2) 실시간 전송
        SseEmitter emitter = emitters.get(receiverId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification") 
                        .data(notification)); 
            } catch (Exception e) { 
                // ★ [수정 포인트] IOException -> Exception으로 변경
                // IllegalStateException (ResponseBodyEmitter가 이미 완료됨) 등을 모두 잡아서 처리
                emitters.remove(receiverId);
                // log.debug("알림 전송 실패(연결 끊김): {}", receiverId); 
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
        
        notification.setChecked(true);
        
        notificationRepository.save(notification);
    }
    
    // 3. 알림 삭제
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));

        if (!notification.getReceiverId().equals(userId)) {
            throw new IllegalStateException("본인의 알림만 삭제할 수 있습니다.");
        }

        notificationRepository.delete(notification);
    }
}
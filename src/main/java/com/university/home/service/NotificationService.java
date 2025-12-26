package com.university.home.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.university.home.dto.NotificationResponseDto;
import com.university.home.entity.CounselingSchedule;
import com.university.home.entity.Notification;
import com.university.home.repository.NotificationRepository;
import com.university.home.repository.ProfessorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; 

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
	
	private final NotificationRepository notificationRepository;
	private final ProfessorRepository professorRepository;
	
	private static final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
	
	
	// 상담 예약 알림
    public void sendAppointmentAlert(CounselingSchedule schedule, String type) {
        Long professorId = schedule.getProfessorId();
        Long studentId = schedule.getStudentId();
        
        String action = switch (type) {
        case "예약" -> "예약했습니다";
        case "예약 취소" -> "예약을 취소했습니다";
        default -> "상태 변경";
    };
    String tab = "학생 상담 목록";
    String url = "/counseling?tab=" + URLEncoder.encode(tab, StandardCharsets.UTF_8);
    String message = String.format("📅 [%s] %s 학생이 상담을 %s. (%s)", 
            type, studentId, action, schedule.getStartTime().toString());
        
        send(professorId, message, url); 
    }
    
    // 클라이언트가 구독(연결) 요청 시 호출
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError((e) -> emitters.remove(userId));

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected!"));
        } catch (IOException e) {
            emitters.remove(userId);
        }

        return emitter;
    }

    // 알림 생성 및 실시간 전송
    @Transactional
    public void send(Long receiverId, String content, String url) { 
        Notification notification = Notification.builder()
                .receiverId(receiverId)
                .content(content)
                .url(url)
                .Checked(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);

        SseEmitter emitter = emitters.get(receiverId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification") 
                        .data(notification)); 
            } catch (Exception e) { 
                emitters.remove(receiverId);
            }
        }
    }
    
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getMyNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByReceiverIdOrderByCreatedAtDesc(userId);
        
        return notifications.stream()
        		.map(this::toResponseDto)
        		.toList();
    }

    //  알림 읽음 처리
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));
        
        notification.setChecked(true);
        
        notificationRepository.save(notification);
    }
    
    // 알림 삭제
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));

        if (!notification.getReceiverId().equals(userId)) {
            throw new IllegalStateException("본인의 알림만 삭제할 수 있습니다.");
        }

        notificationRepository.delete(notification);
    }
    
    // 교수 -> 학생 알림보내기
    @Transactional
    private NotificationResponseDto toResponseDto(Notification notification) {
    	NotificationResponseDto dto = NotificationResponseDto.fromEntity(notification);
    	
    	if("PROFESSOR_MESSAGE".equals(notification.getType()) && notification.getSenderId() != null) {
    		professorRepository.findById(notification.getSenderId())
    		.ifPresent(professor -> dto.setSenderName(professor.getName()));
    	}
    		return dto;
    	}
    	
    public NotificationResponseDto sendDirectMessage(Long senderId, Long receiverId, String content) {
    	
    	Notification notification = Notification.builder()
    			.receiverId(receiverId)
    			.senderId(senderId)
    			.content(content)
    			.type("PROFESSOR_MESSAGE")
    			.url("/counseling")
    			.Checked(false)
    			.createdAt(LocalDateTime.now())
    			.build();
    	
    	
    	Notification savedNotification = notificationRepository.save(notification);
    	
    	NotificationResponseDto responseDto = toResponseDto(savedNotification);
    	
    	SseEmitter emitter = emitters.get(receiverId);
    	if(emitter != null) {
    		try {
				emitter.send(SseEmitter.event()
				.name("notification")
				.data(responseDto));
			} catch (Exception e) {
				emitters.remove(receiverId);
			}
    	}
    	return responseDto;
    }
    
}
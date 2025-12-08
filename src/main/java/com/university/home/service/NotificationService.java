package com.university.home.service;

import org.springframework.stereotype.Service;

import com.university.home.entity.CounselingSchedule;

@Service
public class NotificationService {
    
    public void sendAppointmentAlert(CounselingSchedule schedule, String type) {
        // [TODO: 실제 구현 필요]
        // 1. 알림 메시지 생성 (예: "XX 교수님, YY 학생이 상담을 예약했습니다.")
        // 2. 이메일/시스템 알림 테이블 저장/WebSocket 전송 로직 구현
        System.out.println("🔔 [Notification] " + type + " 알림 - Schedule ID: " + schedule.getId() 
                            + ", Time: " + schedule.getStartTime());
    }
}
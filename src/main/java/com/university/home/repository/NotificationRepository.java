package com.university.home.repository;

import com.university.home.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 교수님의 읽지 않은 알림만 최신순 조회
	List<Notification> findByReceiverIdAndCheckedFalseOrderByCreatedAtDesc(Long receiverId);
	
	List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);
}

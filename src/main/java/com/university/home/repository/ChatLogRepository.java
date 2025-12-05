package com.university.home.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.ChatLog;

public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {
	// 특정 학생의 대화 내역 조회 (최신순) - 프론트에서 채팅창 띄울 때 필요
	List<ChatLog> findByStudentIdOrderByCreatedAtAsc(Long studentId);
	
	//대화 삭제
	void deleteByStudent_Id(Long studentId);
}

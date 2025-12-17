package com.university.home.dto;

import java.time.LocalDateTime;

import com.university.home.entity.Notification;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationResponseDto {

	private Long id;
	private Long receiverId;
	private Long senderId;
	private String senderName; //발신자 이름
	private String type;
	private String content;
	private String url;
	private boolean isRead;
	private LocalDateTime createAt;
	
	public static NotificationResponseDto fromEntity(Notification notification) {
		return NotificationResponseDto.builder()
				.id(notification.getId())
				.receiverId(notification.getReceiverId())
				.senderId(notification.getSenderId())
				.type(notification.getType())
				.content(notification.getContent())
				.url(notification.getUrl())
				.isRead(notification.isRead())
				.createAt(notification.getCreatedAt())
				.build();
	}
	

}

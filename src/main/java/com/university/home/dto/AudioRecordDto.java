package com.university.home.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class AudioRecordDto {
    private Long scheduleId;
    private MultipartFile audioFile; // 클라이언트가 보낸 음성 파일
}
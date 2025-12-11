package com.university.home.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.home.entity.CounselingRecord;
import com.university.home.repository.CounselingRecordRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class TranscriptionResultListener {

    private final AmazonS3 s3Client;
    private final CounselingRecordRepository recordRepository; // JPA Repository 가정

    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    
    // ⭐️ SQS Queue 이름 리스너 설정
    @SqsListener("${aws.sqs.transcribe-queue-name}") 
    public void receiveTranscriptionResult(String message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            // Transcribe 알림은 SNS를 거쳐 SQS에 오므로, 메시지 본문을 파싱
            Map<String, Object> snsMessage = mapper.readValue(message, Map.class);
            String rawMessage = (String) snsMessage.get("Message"); 
            
            // 실제 Transcribe 결과 JSON 파싱
            Map<String, Object> notification = mapper.readValue(rawMessage, Map.class);

            // Transcribe Job 이름 추출 (실제 SQS 메시지 구조를 확인하여 정확한 키를 사용해야 함)
            // AWS 기본 형식에서 jobName을 추출하는 키는 'TranscriptionJobName'일 수 있습니다.
            String jobName = (String) notification.get("TranscriptionJobName"); 
            
            if (jobName == null) {
                 // 🚨 오류 발생 가능성이 높은 부분. 실제 메시지 구조 확인 후 키 변경 필요
                 System.err.println("경고: 메시지에서 TranscriptionJobName 키를 찾을 수 없습니다. 메시지 구조 확인 필요.");
                 return; 
            }
            
            // Job 이름에서 상담 ID 추출 (예: "12345-uuid")
            Long counselingId = Long.parseLong(jobName.split("-")[0]);
            
            // ⭐️ Transcribe 결과 파일 경로 구성 (AWS 기본 규칙: output-bucket/stt-results-prefix/jobName.json)
            String resultKey = "stt-results/" + jobName + ".json"; 
            
            // 1. S3에서 Transcribe 결과 JSON 파일 다운로드
            S3Object object = s3Client.getObject(bucketName, resultKey); 
            String fullTranscript = parseTranscriptFromS3(object); 
            
            // 2. DB 업데이트 (JPA 사용)
            CounselingRecord record = recordRepository.findByScheduleId(counselingId)
                .orElse(new CounselingRecord()); 
                
            // ⭐️ CounselingRecord 엔티티의 notes 필드에 저장
            record.setNotes(fullTranscript); 
            recordRepository.save(record);


        } catch (Exception e) {
            System.err.println("Error processing transcription result: " + e.getMessage());
        }
    }
    
    private String parseTranscriptFromS3(S3Object object) throws IOException {
        // Transcribe JSON 파일을 읽어서 최종 텍스트만 추출하는 로직
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(object.getObjectContent(), "UTF-8"))) {
            String jsonContent = reader.lines().collect(Collectors.joining("\n"));
            
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> result = mapper.readValue(jsonContent, Map.class);
            
            // Transcribe JSON 구조에 따라 파싱
            Map<String, Object> results = (Map<String, Object>) result.get("results");
            java.util.List<Map<String, Object>> transcripts = (java.util.List<Map<String, Object>>) results.get("transcripts");
            
            return (String) transcripts.get(0).get("transcript"); 
        }
    }
}
//package com.university.home.service;
//
//
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.GetObjectRequest;
//import software.amazon.awssdk.services.s3.model.S3Exception;
//import software.amazon.awssdk.core.ResponseInputStream;
//import software.amazon.awssdk.services.s3.model.GetObjectResponse;
//import software.amazon.awssdk.services.sqs.SqsAsyncClient;
//import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
//import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
//import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.university.home.entity.CounselingRecord;
//import com.university.home.entity.CounselingSchedule;
//import com.university.home.repository.CounselingRecordRepository;
//import com.university.home.repository.CounselingScheduleRepository;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.event.EventListener;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.stereotype.Service;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.util.Map;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//import java.io.IOException;
//
//@Service
//@RequiredArgsConstructor
//public class TranscriptionResultListener {
//
//    private final S3Client s3Client; 
//    private final SqsAsyncClient sqsAsyncClient; // ⭐️ 비동기 클라이언트 주입
//    private final CounselingRecordRepository recordRepository;
//    private final CounselingScheduleRepository scheduleRepository;
//
//    @Value("${aws.s3.bucket-name}")
//    private String bucketName;
//    
//    @Value("${aws.sqs.transcribe-queue-name}")
//    private String queueName;
//
//    private String queueUrl;
//    
//    // ⭐️ 비동기 메시지 수신을 위한 스케줄러
//    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//
//
//    // ⭐️ [v2 리스너 로직] ApplicationReadyEvent 발생 시 SQS 리스너 시작
//    @EventListener(ApplicationReadyEvent.class)
//    public void startSqsListener() {
//        try {
//            // 1. Queue URL 조회 (비동기)
//            GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder().queueName(queueName).build();
//            this.queueUrl = sqsAsyncClient.getQueueUrl(getQueueUrlRequest).get().queueUrl();
//            
//            System.out.println("SQS Listener 시작됨. Queue URL: " + this.queueUrl);
//
//            // 2. 주기적으로 메시지 수신 작업 예약 (polling)
//            scheduler.scheduleWithFixedDelay(this::pollAndProcessMessages, 5, 5, TimeUnit.SECONDS);
//
//        } catch (Exception e) {
//            System.err.println("SQS Listener 초기화 오류: " + e.getMessage());
//            // 애플리케이션 시작을 막지 않기 위해 오류 발생해도 예외 throw 안 함
//        }
//    }
//
//    private void pollAndProcessMessages() {
//        if (queueUrl == null) return;
//        
//        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
//                .queueUrl(queueUrl)
//                .maxNumberOfMessages(10)
//                .waitTimeSeconds(10) // Long Polling 활성화
//                .build();
//
//        // 3. 메시지 수신 (비동기) 및 처리
//        sqsAsyncClient.receiveMessage(receiveMessageRequest)
//            .thenAccept(response -> {
//                response.messages().forEach(message -> {
//                    try {
//                        processTranscriptionResult(message.body());
//                        
//                        // 4. 메시지 처리 완료 후 삭제 (비동기)
//                        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
//                                .queueUrl(queueUrl)
//                                .receiptHandle(message.receiptHandle())
//                                .build();
//                        sqsAsyncClient.deleteMessage(deleteMessageRequest);
//                        
//                    } catch (Exception e) {
//                        System.err.println("메시지 처리 오류: " + e.getMessage());
//                        // 메시지 삭제는 생략하여 Visibility Timeout 후 재시도
//                    }
//                });
//            })
//            .exceptionally(e -> {
//                System.err.println("SQS 메시지 수신 오류: " + e.getMessage());
//                return null;
//            });
//    }
//
//
//    private void processTranscriptionResult(String message) throws Exception {
//        // 기존 SQS Listener의 내부 로직 (SNS Envelope 처리)
//        ObjectMapper mapper = new ObjectMapper();
//        Map<String, Object> snsEnvelope = mapper.readValue(message, Map.class);
//        String rawMessage = (String) snsEnvelope.get("Message");
//        if (rawMessage == null) {
//            System.err.println("SQS 메시지에 Message 필드 없음: " + message);
//            return;
//        }
//
//        Map<String, Object> notification = mapper.readValue(rawMessage, Map.class);
//        Map<String, Object> transcriptionJob = (Map<String, Object>) notification.get("TranscriptionJob");
//        if (transcriptionJob == null) {
//            System.err.println("TranscriptionJob 정보 없음");
//            return;
//        }
//
//        String jobName = (String) transcriptionJob.get("TranscriptionJobName");
//        if (jobName == null) {
//            System.err.println("TranscriptionJobName 없음");
//            return;
//        }
//
//        // jobName == schedule-{id}-{uuid}
//        String[] parts = jobName.split("-");
//        if (parts.length < 2) {
//            System.err.println("jobName 포맷 오류: " + jobName);
//            return;
//        }
//        Long scheduleId = Long.parseLong(parts[1]);
//
//        // S3 결과 파일 경로
//        String resultKey = "stt-results/" + jobName + ".json";
//        String fullTranscript = parseTranscriptFromS3(resultKey);
//
//        // schedule 조회
//        CounselingSchedule schedule = scheduleRepository.findById(scheduleId)
//                .orElseThrow(() -> new IllegalStateException("존재하지 않는 scheduleId: " + scheduleId));
//
//        // 기존 record 조회 또는 새 생성 및 저장
//        CounselingRecord record = recordRepository.findByScheduleId(scheduleId)
//                .orElseGet(() -> {
//                    CounselingRecord r = new CounselingRecord();
//                    r.setSchedule(schedule);
//                    r.setConsultationDate(schedule.getStartTime());
//                    r.setStudentId(schedule.getStudentId());
//                    r.setStudentName(""); 
//                    return r;
//                });
//
//        record.setNotes(fullTranscript);
//        recordRepository.save(record);
//        System.out.println("STT 결과 저장 완료 scheduleId=" + scheduleId);
//    }
//    
//    // ⭐️ [v2 변경] parseTranscriptFromS3 메서드 수정
//    private String parseTranscriptFromS3(String resultKey) throws IOException { 
//        
//        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
//            .bucket(bucketName)
//            .key(resultKey)
//            .build();
//            
//        // ResponseInputStream<GetObjectResponse> 객체를 반환합니다.
//        try (ResponseInputStream<GetObjectResponse> objectData = s3Client.getObject(getObjectRequest);
//             BufferedReader reader = new BufferedReader(new InputStreamReader(objectData, "UTF-8"))) {
//
//            String json = reader.lines().collect(Collectors.joining("\n"));
//            ObjectMapper mapper = new ObjectMapper();
//            Map<String, Object> result = mapper.readValue(json, Map.class);
//            Map<String, Object> results = (Map<String, Object>) result.get("results");
//            var transcripts = (java.util.List<Map<String, Object>>) results.get("transcripts");
//            if (transcripts == null || transcripts.isEmpty()) return "";
//            return (String) transcripts.get(0).get("transcript");
//        } catch (S3Exception e) {
//             System.err.println("S3에서 결과 파일 읽기 실패: " + e.getMessage());
//             throw new IOException("S3 결과 파일 읽기 실패", e);
//        }
//    }
//}
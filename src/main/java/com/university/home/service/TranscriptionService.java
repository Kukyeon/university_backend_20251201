package com.university.home.service;


import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.LanguageCode;
import software.amazon.awssdk.services.transcribe.model.Media;
import software.amazon.awssdk.services.transcribe.model.StartTranscriptionJobRequest;
import software.amazon.awssdk.services.transcribe.model.Settings;
import software.amazon.awssdk.services.transcribe.model.GetTranscriptionJobRequest;
import software.amazon.awssdk.services.transcribe.model.TranscriptionJobStatus;

import com.university.home.entity.CounselingSchedule;
import com.university.home.repository.CounselingScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TranscriptionService {

    private final S3Client s3Client; 
    private final TranscribeClient transcribeClient; 
    private final CounselingRecordService counselingRecordService;
    private final CounselingScheduleRepository scheduleRepository;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${app.env:local}")
    private String appEnv; // local or prod
    
    // ⭐️ Execution Role ARN 주입 (v2에서 사용 가능)
    @Value("${aws.transcribe.execution-role-arn}")
    private String transcribeExecutionRoleArn; 

    public String startTranscriptionJob(MultipartFile audioFile, Long scheduleId) throws IOException {
        String jobName = "schedule-" + scheduleId + "-" + UUID.randomUUID();

        // 로컬 모드: AWS 호출 없이 mock 동작
        if ("local".equalsIgnoreCase(appEnv)) {
            CounselingSchedule schedule = scheduleRepository.findById(scheduleId).orElse(null);
            counselingRecordService.saveRecord(scheduleId, "[LOCAL TEST] 자동 생성된 STT 결과", "local-mock");
            return "local-" + jobName;
        }

        // ⭐️ [v2 변경] S3 업로드 로직
        String s3Key = "audio/" + scheduleId + "/" + UUID.randomUUID() + ".mp3";
        String s3Uri = "s3://" + bucketName + "/" + s3Key;

        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(audioFile.getContentType())
                .build(),
            RequestBody.fromBytes(audioFile.getBytes())
        );
        
        Settings jobSettings = Settings.builder().build();

        // ⭐️ [v2 변경] Transcribe Job 시작: Settings에 Role ARN 명확히 지정
        StartTranscriptionJobRequest request = StartTranscriptionJobRequest.builder()
                .transcriptionJobName(jobName)
                .languageCode(LanguageCode.KO_KR) // v2 상수명
                .media(Media.builder().mediaFileUri(s3Uri).build())
                .outputBucketName(bucketName)
                .outputKey("stt-results/")
                .settings(jobSettings) // 빈 Settings 객체 전달
                
                // ⭐️ [변경 사항 2] Transcribe 실행 역할을 최상위 필드로 이동
//                .jobExecutionRoleArn(transcribeExecutionRoleArn) 
                
                .build();

        transcribeClient.startTranscriptionJob(request);
        return jobName;
    }

    public String getTranscriptionJobStatus(String jobName) {
        GetTranscriptionJobRequest req = GetTranscriptionJobRequest.builder() 
            .transcriptionJobName(jobName)
            .build();
            
        TranscriptionJobStatus status = transcribeClient.getTranscriptionJob(req)
            .transcriptionJob()
            .transcriptionJobStatus();
            
        return status.name();
    }
}
package com.university.home.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.amazonaws.services.transcribe.model.*;
import com.amazonaws.services.sqs.AmazonSQS;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TranscriptionService {

    private final AmazonS3 s3Client;
    private final AmazonTranscribe transcribeClient;
    private final AmazonSQS sqsClient;
    
    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.sqs.transcribe-queue-name}")
    private String queueName; 
    
    // ⭐️ SQS ARN 대신 임시 SNS Topic ARN 사용을 위한 상수 정의
    // 🚨 실제 배포 시 이 값을 application.properties에서 주입받거나, AWS 콘솔에서 발급받은 유효한 SNS ARN으로 교체해야 합니다.
    private static final String TEMP_SNS_TOPIC_ARN = "arn:aws:sns:ap-northeast-2:000000000000:TranscribeNotificationTopic"; 

    // 🚨 기존 getQueueArn() 메서드는 SQS ARN을 반환하므로, SNS ARN이 필요한 Transcribe 요청에 부적합합니다.
    // 임시로 SNS ARN을 반환하는 메서드로 대체합니다.
    private String getSnsTopicArn() {
        // 실제 운영 환경에서는 AWS API를 통해 SNS Topic ARN을 가져오거나, 
        // application.properties에서 @Value로 주입받는 것이 권장됩니다.
        // 현재는 컴파일 오류를 피하고 Transcribe Job을 시작하기 위해 상수를 반환합니다.
        return TEMP_SNS_TOPIC_ARN;
    }
    
    // 음성파일 업로드 및 Transcribe 작업 시작
public String startTranscriptionJob(MultipartFile audioFile, Long counselingId) throws IOException {
        
        String s3Key = "audio/" + counselingId + "/" + UUID.randomUUID() + ".mp3";
        String s3Uri = "s3://" + bucketName + "/" + s3Key;
        String jobName = counselingId + "-" + UUID.randomUUID(); 

        // 1. S3에 파일 업로드 (메타데이터 추가)
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(audioFile.getSize());
        metadata.setContentType(audioFile.getContentType()); 
        
        s3Client.putObject(bucketName, s3Key, audioFile.getInputStream(), metadata);

        // 2. Transcribe Job 시작 요청 생성
        StartTranscriptionJobRequest request = new StartTranscriptionJobRequest()
                .withTranscriptionJobName(jobName)
                .withLanguageCode(LanguageCode.KoKR) 
                .withMedia(new Media().withMediaFileUri(s3Uri))
                
                // ⭐️ NotificationConfiguration 및 withNotificationConfiguration()을 완전히 제거
                // SDK v1에서는 Job 완료 알림을 API로 설정하지 않습니다.
                
                // 결과 파일이 저장될 S3 버킷을 지정합니다. (필수)
                .withOutputBucketName(bucketName); 
                
        // ⭐️ S3 버킷 내의 결과 파일 경로 접두사를 지정합니다. (선택 사항이지만 권장)
        // 리스너의 resultKey와 일치해야 합니다.
        // .withOutputKey("stt-results/"); 
        
        transcribeClient.startTranscriptionJob(request);
        
        return jobName;
    }
}

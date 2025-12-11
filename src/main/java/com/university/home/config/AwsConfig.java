//package com.university.home.config;
//
//
//import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
//import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
//import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.s3.S3Client; // v2 S3 클라이언트
//import software.amazon.awssdk.services.sqs.SqsAsyncClient;
//import software.amazon.awssdk.services.sqs.SqsClient; // v2 SQS 클라이언트
//import software.amazon.awssdk.services.transcribe.TranscribeClient;
//
//import java.time.Duration;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//
//@Configuration
//public class AwsConfig {
//
//
//	@Value("${cloud.aws.credentials.access-key}")
//	private String accessKey;
//	
//	
//	@Value("${cloud.aws.credentials.secret-key}")
//	private String secretKey;
//	
//	
//	@Value("${cloud.aws.region.static}")
//	private String region;
//	
//	// ⭐️ [v2 변경] Credential Provider 생성 방식 변경
//	private StaticCredentialsProvider getCredentialsProvider() {
//		return StaticCredentialsProvider.create(
//            AwsBasicCredentials.create(accessKey, secretKey)
//        );
//	}
//	
//	
//	@Bean
//    public S3Client amazonS3Client() {
//        return S3Client.builder()
//                .region(Region.of(region))
//                .credentialsProvider(getCredentialsProvider())
//                .build();
//    }
//	
//    
//	@Bean(destroyMethod = "close")
//	public SqsAsyncClient amazonSqsClient() {
//	    return SqsAsyncClient.builder()
//	            .region(Region.of(region))
//	            .credentialsProvider(getCredentialsProvider())
//	            .httpClientBuilder(NettyNioAsyncHttpClient.builder()
//	                .maxConcurrency(50)
//	                .writeTimeout(Duration.ofSeconds(30)))
//	            .build();
//	}
//
//	
//	 @Bean
//	    public TranscribeClient amazonTranscribeClient() {
//	        return TranscribeClient.builder()
//	                .region(Region.of(region))
//	                .credentialsProvider(getCredentialsProvider())
//	                .build();
//	    }
//}
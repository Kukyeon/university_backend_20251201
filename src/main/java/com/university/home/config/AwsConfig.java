package com.university.home.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.amazonaws.services.transcribe.AmazonTranscribeClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.amazonaws.services.sqs.AmazonSQS; 
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

@Configuration
public class AwsConfig {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    private AWSStaticCredentialsProvider getCredentialsProvider() {
        return new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(accessKey, secretKey)
        );
    }

    @Bean
    public AmazonS3 amazonS3Client() {
        return AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(getCredentialsProvider())
                .build();
    }
    
    @Bean
    public AmazonSQS amazonSqsClient() {
        return AmazonSQSClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(getCredentialsProvider())
                .build();
    }
    
    @Bean
    public AmazonTranscribe amazonTranscribeClient() {
        return AmazonTranscribeClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(getCredentialsProvider())
                .build();
    }
}
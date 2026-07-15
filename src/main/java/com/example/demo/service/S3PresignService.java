package com.example.demo.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

@Service
public class S3PresignService {

    @Value("${livekit.s3.access-key}")
    private String accessKey;

    @Value("${livekit.s3.secret-key}")
    private String secretKey;

    @Value("${livekit.s3.region}")
    private String region;

    @Value("${livekit.s3.presign-ttl-minutes:60}")
    private long presignTtlMinutes;

    private S3Presigner presigner;

    @PostConstruct
    public void init() {
        this.presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    @PreDestroy
    public void destroy() {
        if (presigner != null) {
            presigner.close();
        }
    }

    public String createGetObjectUrl(String bucket, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .responseContentType("video/mp4")
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(getPresignTtlMinutes()))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presigned = presigner.presignGetObject(presignRequest);
        return presigned.url().toString();
    }

    public long getPresignTtlMinutes() {
        return Math.max(1, Math.min(presignTtlMinutes, 7 * 24 * 60));
    }
}

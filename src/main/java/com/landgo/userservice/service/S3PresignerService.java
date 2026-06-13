package com.landgo.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3PresignerService {

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.images-bucket}")
    private String bucketName;

    public String generatePresignedReadUrl(String fileKey, int expiryMinutes) {
        if (fileKey == null || fileKey.isBlank()) {
            return null;
        }
        try {
            int clampedExpiry = Math.min(Math.max(expiryMinutes, 1), 1440); // clamp to max 24 hours

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(clampedExpiry))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            log.debug("Generated presigned read URL for fileKey: {} in user-service", fileKey);
            return presignedRequest.url().toString();
        } catch (Exception e) {
            log.error("Failed to generate presigned read URL for fileKey: {}", fileKey, e);
            return null;
        }
    }
}

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

    /**
     * Turns a stored media reference into a URL a browser can actually load.
     *
     * <p>The images bucket is private, so a bare {@code https://bucket.s3.region.amazonaws.com/key}
     * URL — which is what several code paths persisted — answers 403 with an XML body. Browsers
     * and Next.js image optimisers report that as a broken image rather than a permissions
     * problem, which is why this read as corrupt uploads. Anything pointing at our own bucket is
     * therefore re-signed here, whether it was stored as a key or as a full URL.
     *
     * <p>A genuinely external URL (a Google or Apple avatar from OAuth sign-in) is returned
     * untouched — it is already public and signing it would be meaningless.
     *
     * @param stored a bare S3 key, a bucket URL, or an external URL; may be null
     * @return a signed, loadable URL, the original external URL, or null
     */
    public String toViewableUrl(String stored, int expiryMinutes) {
        if (stored == null || stored.isBlank()) {
            return null;
        }
        String value = stored.trim();

        String key = extractOwnBucketKey(value);
        if (key != null) {
            String signed = generatePresignedReadUrl(key, expiryMinutes);
            return signed != null ? signed : value;
        }
        return value;
    }

    /**
     * The object key when {@code value} refers to our images bucket, otherwise null.
     *
     * <p>Handles both S3 URL styles — {@code bucket.s3.region.amazonaws.com/key} and
     * {@code s3.region.amazonaws.com/bucket/key} — plus a bare key.
     */
    private String extractOwnBucketKey(String value) {
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            return value;
        }
        if (bucketName == null || bucketName.isBlank() || !value.contains(bucketName)) {
            return null;
        }
        try {
            java.net.URI uri = java.net.URI.create(value);
            String host = uri.getHost() == null ? "" : uri.getHost();
            String path = uri.getPath() == null ? "" : uri.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            if (host.startsWith(bucketName + ".")) {
                return path.isBlank() ? null : path;
            }
            String prefix = bucketName + "/";
            if (path.startsWith(prefix)) {
                String key = path.substring(prefix.length());
                return key.isBlank() ? null : key;
            }
            return null;
        } catch (IllegalArgumentException e) {
            log.warn("Could not parse stored media URL: {}", value);
            return null;
        }
    }

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

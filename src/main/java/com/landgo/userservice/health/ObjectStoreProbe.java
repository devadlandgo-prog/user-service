package com.landgo.userservice.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Object storage probe: AWS S3 object storage.
 *
 * <p>Writes a small probe object, reads it back, compares the bytes, then
 * deletes it. A read-only check is not enough -- reads keep succeeding long
 * after writes have broken, which is the failure that actually matters for
 * uploads.
 *
 * <p>Legs generated from discovered IAM permissions: write, read, delete.
 */
@Component
public class ObjectStoreProbe implements DeepHealthProbe {

    private static final Logger log = LoggerFactory.getLogger(ObjectStoreProbe.class);

    private static final String KEY_PREFIX = "health-check/user-service/";

    private final S3Client s3Client;

    @Value("${aws.s3.images-bucket}")
    private String bucket;

    public ObjectStoreProbe(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String name() {
        return "s3";
    }

    @Override
    public ProbeResult check() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("bucket", bucket);

        String key = KEY_PREFIX + UUID.randomUUID() + ".txt";
        String payload = "health-check " + Instant.now();
        long start = System.nanoTime();
        boolean deleted = false;

        try {
            long writeStart = System.nanoTime();
            s3Client.putObject(
                    PutObjectRequest.builder().bucket(bucket).key(key).build(),
                    RequestBody.fromString(payload, StandardCharsets.UTF_8));
            details.put("writeMs", millisSince(writeStart));
            details.put("write", true);

            long readStart = System.nanoTime();
            ResponseBytes<GetObjectResponse> object = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder().bucket(bucket).key(key).build());
            String roundTripped = object.asString(StandardCharsets.UTF_8);
            details.put("readMs", millisSince(readStart));
            details.put("read", true);

            if (!payload.equals(roundTripped)) {
                return ProbeResult.down(name(), millisSince(start),
                        "object read back did not match what was written", details);
            }

            long deleteStart = System.nanoTime();
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
            details.put("deleteMs", millisSince(deleteStart));
            details.put("delete", true);
            deleted = true;

            return ProbeResult.up(name(), millisSince(start), details);
        } catch (Exception e) {
            log.warn("Object store probe failed for bucket {}", bucket, e);
            return ProbeResult.down(name(), millisSince(start), e.toString(), details);
        } finally {

            if (!deleted) {
                try {
                    s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
                } catch (Exception cleanup) {
                    log.warn("Probe object {} left behind in {}", key, bucket, cleanup);
                }
            }
        }
    }

    private static long millisSince(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }
}

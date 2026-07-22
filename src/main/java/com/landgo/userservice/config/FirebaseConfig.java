package com.landgo.userservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account.base64:}")
    private String firebaseServiceAccountBase64;

    @Value("${firebase.service-account.json:}")
    private String firebaseServiceAccountJson;

    @Value("${firebase.service-account.path:}")
    private String firebaseServiceAccountPath;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccountStream = null;

                if (firebaseServiceAccountBase64 != null && !firebaseServiceAccountBase64.isBlank()) {
                    log.info("Initializing Firebase using Base64 service account string");
                    byte[] decodedBytes = Base64.getDecoder().decode(firebaseServiceAccountBase64.trim());
                    serviceAccountStream = new ByteArrayInputStream(decodedBytes);
                } else if (firebaseServiceAccountJson != null && !firebaseServiceAccountJson.isBlank()) {
                    log.info("Initializing Firebase using raw JSON service account string");
                    serviceAccountStream = new ByteArrayInputStream(firebaseServiceAccountJson.trim().getBytes(StandardCharsets.UTF_8));
                } else if (firebaseServiceAccountPath != null && !firebaseServiceAccountPath.isBlank()) {
                    log.info("Initializing Firebase using file path: {}", firebaseServiceAccountPath);
                    serviceAccountStream = new FileInputStream(firebaseServiceAccountPath.trim());
                } else {
                    InputStream classpathStream = getClass().getClassLoader().getResourceAsStream("firebase-service-account.json");
                    if (classpathStream != null) {
                        log.info("Initializing Firebase using classpath resource: firebase-service-account.json");
                        serviceAccountStream = classpathStream;
                    }
                }

                if (serviceAccountStream != null) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                            .build();

                    FirebaseApp.initializeApp(options);
                    log.info("Firebase Admin SDK application initialized successfully (Project: landgo-71b63)");
                } else {
                    log.warn("Firebase service account credentials missing. Push notifications will be disabled until credentials are provided.");
                }
            }
        } catch (Exception e) {
            log.error("Failed to initialize Firebase App", e);
        }
    }
}

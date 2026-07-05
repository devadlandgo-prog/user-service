package com.landgo.userservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account.base64:}")
    private String firebaseServiceAccountBase64;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                if (firebaseServiceAccountBase64 != null && !firebaseServiceAccountBase64.isBlank()) {
                    byte[] decodedBytes = Base64.getDecoder().decode(firebaseServiceAccountBase64);
                    InputStream serviceAccount = new ByteArrayInputStream(decodedBytes);

                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();

                    FirebaseApp.initializeApp(options);
                    log.info("Firebase application has been initialized");
                } else {
                    log.warn("Firebase service account credentials missing. Push notifications will not work.");
                }
            }
        } catch (Exception e) {
            log.error("Failed to initialize Firebase App", e);
        }
    }
}

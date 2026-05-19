package com.landgo.userservice.config;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.landgo.userservice.oauth.GoogleTokenVerifier;
import com.landgo.userservice.oauth.GoogleTokenVerifierImpl;
import com.landgo.userservice.service.SecretsManagerService;

@Configuration
@EnableConfigurationProperties(GoogleOAuthProperties.class)
public class GoogleOAuthConfig {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @Bean
    public GoogleTokenVerifier googleTokenVerifier(GoogleOAuthProperties properties,
                                                   ObjectProvider<SecretsManagerService> secretsManagerServiceProvider)
            throws GeneralSecurityException, IOException {

        SecretsManagerService secretsManagerService = secretsManagerServiceProvider.getIfAvailable();
        if (properties.getSecretName() != null && !properties.getSecretName().isBlank() && secretsManagerService != null) {
            properties.applySecret(secretsManagerService.fetchSecret(properties.getSecretName()));
            properties.resetResolvedAudiences();
        }

        List<String> audiences = properties.resolveAudiences();
        if (CollectionUtils.isEmpty(audiences)) {
            throw new IllegalStateException("No Google OAuth audiences configured. Please set client IDs or audiences.");
        }

        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, JSON_FACTORY)
                .setAudience(audiences)
                .build();
        return new GoogleTokenVerifierImpl(verifier, properties);
    }
}

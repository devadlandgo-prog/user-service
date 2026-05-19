package com.landgo.userservice.config;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.landgo.userservice.exception.BadRequestException;
import com.landgo.userservice.oauth.GoogleTokenVerifier;
import com.landgo.userservice.oauth.GoogleTokenVerifierImpl;
import com.landgo.userservice.service.SecretsManagerService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

@Configuration
@EnableConfigurationProperties(GoogleOAuthProperties.class)
public class GoogleOAuthConfig {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Bean
    @ConditionalOnExpression("'${app.oauth2.google.client-id:}' != ''")
    public GoogleTokenVerifier googleTokenVerifier(GoogleOAuthProperties properties,
                                                   ObjectProvider<SecretsManagerService> secretsManagerServiceProvider)
            throws GeneralSecurityException, IOException {

        SecretsManagerService secretsManagerService = secretsManagerServiceProvider.getIfAvailable();
        if (properties.getSecretName() != null && !properties.getSecretName().isBlank() && secretsManagerService != null) {
            properties.applySecret(secretsManagerService.fetchSecret(properties.getSecretName()));
            properties.resetResolvedAudiences();
        }

        List<String> audiences = properties.resolveAudiences();
        if (audiences.isEmpty()) {
            throw new IllegalStateException("No Google OAuth audiences configured. Please set client IDs or audiences.");
        }

        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, JSON_FACTORY)
                .setAudience(audiences)
                .build();
        return new GoogleTokenVerifierImpl(verifier, properties);
    }

    @Bean
    @ConditionalOnMissingBean(GoogleTokenVerifier.class)
    public GoogleTokenVerifier noOpGoogleTokenVerifier() {
        return token -> {
            throw new BadRequestException("Google OAuth is not configured", "AUTH_GOOGLE_NOT_CONFIGURED");
        };
    }
}

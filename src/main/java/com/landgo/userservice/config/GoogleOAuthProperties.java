package com.landgo.userservice.config;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.oauth2.google")
public class GoogleOAuthProperties {

    private String clientId;
    private String webClientId;
    private String androidClientId;
    private String iosClientId;
    private List<String> additionalAudiences = new ArrayList<>();
    private String secretName;

    private final Set<String> resolvedAudiences = new LinkedHashSet<>();

    public void applySecret(Map<String, String> secretMap) {
        if (secretMap == null) {
            return;
        }
        String clientIdValue = getSecretValue(secretMap, "clientId", "client_id", "googleClientId");
        if (StringUtils.hasText(clientIdValue)) {
            this.clientId = clientIdValue.trim();
        }

        String webClientIdValue = getSecretValue(secretMap, "webClientId", "web_client_id", "googleWebClientId");
        if (StringUtils.hasText(webClientIdValue)) {
            this.webClientId = webClientIdValue.trim();
        }

        String androidClientIdValue = getSecretValue(secretMap, "androidClientId", "android_client_id", "googleAndroidClientId");
        if (StringUtils.hasText(androidClientIdValue)) {
            this.androidClientId = androidClientIdValue.trim();
        }

        String iosClientIdValue = getSecretValue(secretMap, "iosClientId", "ios_client_id", "googleIosClientId");
        if (StringUtils.hasText(iosClientIdValue)) {
            this.iosClientId = iosClientIdValue.trim();
        }

        String additionalAudiencesValue = getSecretValue(secretMap, "additionalAudiences", "additional_audiences", "googleAdditionalAudiences");
        if (StringUtils.hasText(additionalAudiencesValue)) {
            String value = additionalAudiencesValue;
            String[] parts = value.split(",");
            additionalAudiences.clear();
            for (String part : parts) {
                if (StringUtils.hasText(part)) {
                    additionalAudiences.add(part.trim());
                }
            }
        }

        // allow overriding secret name chaining in case of rotation
        String secretNameValue = getSecretValue(secretMap, "secretName", "secret_name", "googleOauthSecretName");
        if (StringUtils.hasText(secretNameValue)) {
            this.secretName = secretNameValue.trim();
        }

        resetResolvedAudiences();
    }

    public List<String> resolveAudiences() {
        if (resolvedAudiences.isEmpty()) {
            addIfPresent(clientId);
            addIfPresent(webClientId);
            addIfPresent(androidClientId);
            addIfPresent(iosClientId);
            if (additionalAudiences != null) {
                for (String audience : additionalAudiences) {
                    addIfPresent(audience);
                }
            }
        }
        return List.copyOf(resolvedAudiences);
    }

    public boolean hasAudiences() {
        return !resolveAudiences().isEmpty();
    }

    private void addIfPresent(String candidate) {
        if (StringUtils.hasText(candidate)) {
            resolvedAudiences.add(candidate.trim());
        }
    }

    public void resetResolvedAudiences() {
        resolvedAudiences.clear();
    }

    private String getSecretValue(Map<String, String> secretMap, String... keys) {
        for (String key : keys) {
            String value = secretMap.get(key);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }
}

package com.landgo.userservice.service;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PreDestroy;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

@Service
public class SecretsManagerService implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(SecretsManagerService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SecretsManagerClient client;

    public SecretsManagerService(@Value("${aws.region:}") String region) {
        SecretsManagerClientBuilder builder = SecretsManagerClient.builder();
        if (StringUtils.hasText(region)) {
            builder = builder.region(Region.of(region));
        }
        this.client = builder.build();
    }

    public Map<String, String> fetchSecret(String secretId) {
        if (!StringUtils.hasText(secretId)) {
            return Collections.emptyMap();
        }
        try {
            GetSecretValueResponse response = client.getSecretValue(
                    GetSecretValueRequest.builder().secretId(secretId).build());
            if (!StringUtils.hasText(response.secretString())) {
                return Collections.emptyMap();
            }
            return objectMapper.readValue(response.secretString(), new TypeReference<Map<String, String>>() {});
        } catch (SecretsManagerException e) {
            log.error("Failed to fetch secret '{}' from AWS Secrets Manager: {}", secretId, e.awsErrorDetails().errorMessage());
            throw e;
        } catch (IOException e) {
            log.error("Failed to parse secret '{}' from AWS Secrets Manager", secretId, e);
            throw new RuntimeException("Unable to parse Secrets Manager payload", e);
        }
    }

    @PreDestroy
    @Override
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}

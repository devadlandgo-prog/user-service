package com.landgo.userservice.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.landgo.userservice.dto.request.RegisterRequest;
import com.landgo.userservice.enums.AuthProvider;
import com.landgo.userservice.exception.BadRequestException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AppleAuthenticationStrategy implements OAuth2AuthenticationStrategy {

    private static final String APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys";

    @Value("${app.oauth2.apple.client-id:}")
    private String appleClientId;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AppleAuthenticationStrategy(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public AuthProvider getProvider() { return AuthProvider.APPLE; }

    @Override
    @SuppressWarnings("unchecked")
    public OAuth2UserInfo extractUserInfo(String token) {
        try {
            Map<String, Object> keysResponse = restTemplate.getForObject(APPLE_KEYS_URL, Map.class);
            List<Map<String, String>> keys = (List<Map<String, String>>) keysResponse.get("keys");

            String[] tokenParts = token.split("\\.");
            String header = new String(Base64.getUrlDecoder().decode(tokenParts[0]));
            Map<String, String> headerMap = objectMapper.readValue(header, Map.class);
            String kid = headerMap.get("kid");

            Map<String, String> matchingKey = keys.stream()
                    .filter(key -> key.get("kid").equals(kid))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Apple key not found"));

            PublicKey publicKey = buildPublicKey(matchingKey);

            Claims claims = Jwts.parser().verifyWith(publicKey).build()
                    .parseSignedClaims(token).getPayload();

            String aud = claims.getAudience().iterator().next();
            if (!appleClientId.equals(aud)) throw new BadRequestException("Invalid Apple token audience");

            return OAuth2UserInfo.builder()
                    .providerId(claims.getSubject())
                    .email(claims.get("email", String.class))
                    .firstName("").lastName("").build();
        } catch (BadRequestException e) { throw e; }
        catch (Exception e) {
            log.error("Failed to verify Apple token", e);
            throw new BadRequestException("Failed to verify Apple token: " + e.getMessage());
        }
    }

    private PublicKey buildPublicKey(Map<String, String> key) throws Exception {
        byte[] nBytes = Base64.getUrlDecoder().decode(key.get("n"));
        byte[] eBytes = Base64.getUrlDecoder().decode(key.get("e"));
        RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(1, nBytes), new BigInteger(1, eBytes));
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    @Override
    public RegisterRequest toRegisterRequest(OAuth2UserInfo userInfo) {
        String first = userInfo.getFirstName().isEmpty() ? "Apple" : userInfo.getFirstName();
        String last = userInfo.getLastName().isEmpty() ? "User" : userInfo.getLastName();
        return RegisterRequest.builder()
                .fullName((first + " " + last).trim()).role("seller")
                .email(userInfo.getEmail())
                .profileImageUrl(userInfo.getProfileImageUrl())
                .authProvider(AuthProvider.APPLE).providerId(userInfo.getProviderId()).build();
    }
}

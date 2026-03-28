package com.landgo.userservice.strategy;

import com.landgo.userservice.dto.request.RegisterRequest;
import com.landgo.userservice.enums.AuthProvider;
import com.landgo.userservice.enums.UserType;
import com.landgo.userservice.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class GoogleAuthenticationStrategy implements OAuth2AuthenticationStrategy {

    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    @Value("${app.oauth2.google.client-id:}")
    private String googleClientId;

    private final RestTemplate restTemplate;

    public GoogleAuthenticationStrategy(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public AuthProvider getProvider() { return AuthProvider.GOOGLE; }

    @Override
    @SuppressWarnings("unchecked")
    public OAuth2UserInfo extractUserInfo(String token) {
        try {
            Map<String, Object> response = restTemplate.getForObject(GOOGLE_TOKEN_INFO_URL + token, Map.class);
            if (response == null) throw new BadRequestException("Invalid Google token");

            String aud = (String) response.get("aud");
            if (!googleClientId.equals(aud)) {
                log.warn("Google token audience mismatch: expected {}, got {}", googleClientId, aud);
                throw new BadRequestException("Invalid Google token audience");
            }

            return OAuth2UserInfo.builder()
                    .providerId((String) response.get("sub"))
                    .email((String) response.get("email"))
                    .firstName((String) response.getOrDefault("given_name", ""))
                    .lastName((String) response.getOrDefault("family_name", ""))
                    .profileImageUrl((String) response.get("picture"))
                    .build();
        } catch (BadRequestException e) { throw e; }
        catch (Exception e) {
            log.error("Failed to verify Google token", e);
            throw new BadRequestException("Failed to verify Google token: " + e.getMessage());
        }
    }

    @Override
    public RegisterRequest toRegisterRequest(OAuth2UserInfo userInfo) {
        String fullName = (userInfo.getFirstName() + " " + userInfo.getLastName()).trim();
        if (fullName.isEmpty()) fullName = userInfo.getEmail().split("@")[0];
        return RegisterRequest.builder()
                .userType(UserType.SELLER).fullName(fullName).email(userInfo.getEmail())
                .firstName(userInfo.getFirstName()).lastName(userInfo.getLastName())
                .profileImageUrl(userInfo.getProfileImageUrl())
                .authProvider(AuthProvider.GOOGLE).providerId(userInfo.getProviderId()).build();
    }
}

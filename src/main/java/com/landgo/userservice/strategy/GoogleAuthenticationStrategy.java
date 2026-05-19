package com.landgo.userservice.strategy;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.landgo.userservice.dto.request.RegisterRequest;
import com.landgo.userservice.enums.AuthProvider;
import com.landgo.userservice.exception.BadRequestException;
import com.landgo.userservice.oauth.GoogleTokenVerifier;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GoogleAuthenticationStrategy implements OAuth2AuthenticationStrategy {

    private final GoogleTokenVerifier tokenVerifier;

    public GoogleAuthenticationStrategy(GoogleTokenVerifier tokenVerifier) {
        this.tokenVerifier = tokenVerifier;
    }

    @Override
    public AuthProvider getProvider() { return AuthProvider.GOOGLE; }

    @Override
    public OAuth2UserInfo extractUserInfo(String token) {
        try {
            GoogleIdToken.Payload payload = tokenVerifier.verify(token);
            String providerId = payload.getSubject();
            String email = payload.getEmail();
            Boolean emailVerified = payload.getEmailVerified();

            if (providerId == null || providerId.isBlank()) {
                throw new BadRequestException("Google token missing subject", "AUTH_GOOGLE_INVALID_TOKEN");
            }
            if (email == null || email.isBlank()) {
                throw new BadRequestException("Google token missing email", "AUTH_GOOGLE_INVALID_TOKEN");
            }
            if (Boolean.FALSE.equals(emailVerified)) {
                throw new BadRequestException("Google account email is not verified", "AUTH_GOOGLE_EMAIL_NOT_VERIFIED");
            }

            return OAuth2UserInfo.builder()
                    .providerId(providerId)
                    .email(email.toLowerCase())
                    .firstName((String) payload.getOrDefault("given_name", ""))
                    .lastName((String) payload.getOrDefault("family_name", ""))
                    .profileImageUrl((String) payload.get("picture"))
                    .build();
        } catch (BadRequestException e) { throw e; }
        catch (Exception e) {
            log.error("Failed to verify Google token", e);
            throw new BadRequestException("Failed to verify Google token: " + e.getMessage(), "AUTH_GOOGLE_TOKEN_VERIFICATION_FAILED");
        }
    }

    @Override
    public RegisterRequest toRegisterRequest(OAuth2UserInfo userInfo) {
        String fullName = (userInfo.getFirstName() + " " + userInfo.getLastName()).trim();
        if (fullName.isEmpty()) fullName = userInfo.getEmail().split("@")[0];
        return RegisterRequest.builder()
                .fullName(fullName).email(userInfo.getEmail()).role("seller")
                .profileImageUrl(userInfo.getProfileImageUrl())
                .authProvider(AuthProvider.GOOGLE).providerId(userInfo.getProviderId()).build();
    }
}

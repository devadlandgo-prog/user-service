package com.landgo.userservice.oauth;

import java.util.List;

import org.springframework.util.CollectionUtils;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.landgo.userservice.config.GoogleOAuthProperties;
import com.landgo.userservice.exception.BadRequestException;

public class GoogleTokenVerifierImpl implements GoogleTokenVerifier {

    private final GoogleIdTokenVerifier delegate;
    private final GoogleOAuthProperties properties;

    public GoogleTokenVerifierImpl(GoogleIdTokenVerifier delegate, GoogleOAuthProperties properties) {
        this.delegate = delegate;
        this.properties = properties;
    }

    @Override
    public GoogleIdToken.Payload verify(String idToken) {
        try {
            GoogleIdToken googleIdToken = delegate.verify(idToken);
            if (googleIdToken == null) {
                throw new BadRequestException("Invalid Google ID token", "AUTH_GOOGLE_INVALID_TOKEN");
            }
            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            validateAudience(payload);
            return payload;
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Failed to verify Google token: " + e.getMessage(), "AUTH_GOOGLE_TOKEN_VERIFICATION_FAILED");
        }
    }

    private void validateAudience(GoogleIdToken.Payload payload) {
        List<String> audiences = properties.resolveAudiences();
        if (CollectionUtils.isEmpty(audiences)) {
            return;
        }
        Object audClaim = payload.getAudience();
        if (audClaim instanceof String singleAudience) {
            if (!audiences.contains(singleAudience)) {
                throw new BadRequestException("Google token audience mismatch", "AUTH_GOOGLE_AUDIENCE_MISMATCH");
            }
        } else if (audClaim instanceof List<?> audList) {
            boolean anyMatch = audList.stream().anyMatch(entry -> entry instanceof String && audiences.contains(entry));
            if (!anyMatch) {
                throw new BadRequestException("Google token audience mismatch", "AUTH_GOOGLE_AUDIENCE_MISMATCH");
            }
        }
    }
}

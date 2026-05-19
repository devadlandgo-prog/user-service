package com.landgo.userservice.oauth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

public interface GoogleTokenVerifier {
    GoogleIdToken.Payload verify(String idToken);
}

package com.landgo.userservice.strategy;

import com.landgo.userservice.dto.request.RegisterRequest;
import com.landgo.userservice.enums.AuthProvider;

public interface OAuth2AuthenticationStrategy {
    AuthProvider getProvider();
    OAuth2UserInfo extractUserInfo(String token);
    RegisterRequest toRegisterRequest(OAuth2UserInfo userInfo);
}

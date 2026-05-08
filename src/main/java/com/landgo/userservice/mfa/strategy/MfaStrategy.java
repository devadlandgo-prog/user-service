package com.landgo.userservice.mfa.strategy;

import com.landgo.userservice.entity.User;

public interface MfaStrategy {
    void sendCode(User user);
    boolean verifyCode(User user, String code);
    String getMethodName();
}

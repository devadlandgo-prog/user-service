package com.landgo.userservice.mfa.strategy;

import com.landgo.userservice.entity.User;
import com.landgo.userservice.service.TwilioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TwilioSmsMfaStrategy implements MfaStrategy {

    private final TwilioService twilioService;

    @Override
    public void sendCode(User user) {
        twilioService.sendVerificationCode(user.getPhone());
    }

    @Override
    public boolean verifyCode(User user, String code) {
        return twilioService.checkVerificationCode(user.getPhone(), code);
    }

    @Override
    public String getMethodName() {
        return "SMS";
    }
}

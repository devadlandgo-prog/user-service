package com.landgo.userservice.service;

import com.landgo.userservice.config.TwilioConfig;
import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwilioService {

    private final TwilioConfig twilioConfig;

    @PostConstruct
    public void init() {
        Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());
        log.info("Twilio initialized with Account SID: {}", twilioConfig.getAccountSid());
    }

    public void sendVerificationCode(String phoneNumber) {
        log.info("Sending verification code to: {}", phoneNumber);
        Verification.creator(
                twilioConfig.getVerifyServiceSid(),
                phoneNumber,
                "sms"
        ).create();
    }

    public boolean checkVerificationCode(String phoneNumber, String code) {
        log.info("Checking verification code for: {}", phoneNumber);
        try {
            VerificationCheck check = VerificationCheck.creator(
                    twilioConfig.getVerifyServiceSid()
            ).setTo(phoneNumber).setCode(code).create();
            return "approved".equalsIgnoreCase(check.getStatus());
        } catch (Exception e) {
            log.error("Error checking verification code", e);
            return false;
        }
    }
}

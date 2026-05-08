package com.landgo.userservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "twilio")
public class TwilioConfig {
    private String accountSid;
    private String authToken;
    private String verifyServiceSid;
}

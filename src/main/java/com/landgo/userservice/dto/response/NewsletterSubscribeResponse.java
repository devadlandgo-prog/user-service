package com.landgo.userservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class NewsletterSubscribeResponse {
    private boolean success;
    private UUID subscriberId;
    private String message;
}

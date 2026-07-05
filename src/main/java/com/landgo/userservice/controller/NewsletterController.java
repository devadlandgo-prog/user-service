package com.landgo.userservice.controller;

import com.landgo.userservice.dto.response.ApiResponse;
import com.landgo.userservice.dto.request.NewsletterSubscribeRequest;
import com.landgo.userservice.dto.response.NewsletterSubscribeResponse;
import com.landgo.userservice.service.NewsletterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/newsletter")
@RequiredArgsConstructor
public class NewsletterController {

    private final NewsletterService newsletterService;

    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<NewsletterSubscribeResponse>> subscribe(
            @Valid @RequestBody NewsletterSubscribeRequest request) {
        
        NewsletterSubscribeResponse response = newsletterService.subscribe(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<ApiResponse<Void>> unsubscribe(@RequestParam String email) {
        newsletterService.unsubscribe(email);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

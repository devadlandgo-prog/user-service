package com.landgo.userservice.controller;

import com.landgo.userservice.dto.response.ApiResponse;
import com.landgo.userservice.dto.request.NewsletterSubscribeRequest;
import com.landgo.userservice.dto.response.NewsletterSubscribeResponse;
import com.landgo.userservice.exception.BadRequestException;
import com.landgo.userservice.service.NewsletterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/newsletter")
@RequiredArgsConstructor
@Tag(name = "Newsletter", description = "Public newsletter subscription and unsubscribe endpoints")
public class NewsletterController {

    private final NewsletterService newsletterService;

    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe to the newsletter", description = "Idempotent — re-subscribing a known email reactivates it rather than erroring.")
    public ResponseEntity<ApiResponse<NewsletterSubscribeResponse>> subscribe(
            @Valid @RequestBody NewsletterSubscribeRequest request) {
        
        NewsletterSubscribeResponse response = newsletterService.subscribe(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Unsubscribes by opaque token (the link carried in every newsletter) or by email address.
     * Exactly one of the two must be supplied.
     */
    @PostMapping("/unsubscribe")
    @Operation(summary = "Unsubscribe from the newsletter", description = "Pass either ?token=<UUID> (from the email footer link) or ?email=<address>. Exactly one is required.")
    public ResponseEntity<ApiResponse<Void>> unsubscribe(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String token) {

        if (token != null && !token.isBlank()) {
            newsletterService.unsubscribeByToken(token);
        } else if (email != null && !email.isBlank()) {
            newsletterService.unsubscribe(email);
        } else {
            throw new BadRequestException("Either 'token' or 'email' is required", "VALIDATION_ERROR");
        }

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /** GET variant so the unsubscribe link in an email works when clicked directly. */
    @GetMapping("/unsubscribe")
    @Operation(summary = "Unsubscribe via email link (GET)", description = "Token-based unsubscribe using GET so email client link clicks work without JavaScript.")
    public ResponseEntity<ApiResponse<Void>> unsubscribeViaLink(@RequestParam String token) {
        newsletterService.unsubscribeByToken(token);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

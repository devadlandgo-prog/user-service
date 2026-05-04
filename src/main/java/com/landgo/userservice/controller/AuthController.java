package com.landgo.userservice.controller;

import com.landgo.userservice.dto.request.*;
import com.landgo.userservice.dto.response.ApiResponse;
import com.landgo.userservice.dto.response.AuthResponse;
import com.landgo.userservice.dto.response.UserResponse;
import com.landgo.userservice.security.CurrentUser;
import com.landgo.userservice.security.UserPrincipal;
import com.landgo.userservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration, login, OAuth2, email verification, password management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Received registration request: {}", request);
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful. Please verify your email.", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/oauth2")
    @Operation(summary = "OAuth2 login (Google/Apple)")
    public ResponseEntity<ApiResponse<AuthResponse>> oauth2Login(@Valid @RequestBody OAuth2Request request) {
        AuthResponse response = authService.oauth2Login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }



    @PostMapping("/logout")
    @Operation(summary = "Logout current user")
    public ResponseEntity<ApiResponse<Void>> logout(@CurrentUser UserPrincipal userPrincipal) {
        authService.logout(userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify email with 4-digit code")
    public ResponseEntity<ApiResponse<UserResponse>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        UserResponse response = authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", response));
    }

    @GetMapping("/verify-link")
    @Operation(summary = "Verify email with one-click token link")
    public ResponseEntity<ApiResponse<UserResponse>> verifyEmailByLink(@RequestParam String token) {
        UserResponse response = authService.verifyEmailByToken(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", response));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend email verification code")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerificationCode(request);
        return ResponseEntity.ok(ApiResponse.success("Verification code sent", null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("If an account exists, a reset link has been sent", null));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current logged in user")
    public ResponseEntity<ApiResponse<UserResponse>> me(@CurrentUser UserPrincipal userPrincipal) {
        UserResponse response = authService.getCurrentUser(userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/reset-password/validate")
    @Operation(summary = "Validate password reset token")
    public ResponseEntity<ApiResponse<Void>> validateResetToken(@RequestParam String token) {
        authService.validateResetToken(token);
        return ResponseEntity.ok(ApiResponse.success("Token is valid", null));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshAccessToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password for logged-in user")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userPrincipal, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @PostMapping("/mfa/verify")
    @Operation(summary = "Verify MFA code to complete login")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyMfa(@Valid @RequestBody MfaVerifyRequest request) {
        AuthResponse response = authService.verifyMfa(request);
        return ResponseEntity.ok(ApiResponse.success("MFA verified successfully", response));
    }

    @PostMapping("/mfa/resend")
    @Operation(summary = "Resend MFA code")
    public ResponseEntity<ApiResponse<Void>> resendMfa(@Valid @RequestBody MfaResendRequest request) {
        authService.resendMfa(request);
        return ResponseEntity.ok(ApiResponse.success("MFA code resent successfully", null));
    }
}

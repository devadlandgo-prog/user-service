package com.landgo.userservice.service;

import com.landgo.userservice.dto.request.*;
import com.landgo.userservice.dto.response.AuthResponse;
import com.landgo.userservice.dto.response.UserResponse;
import com.landgo.userservice.entity.EmailVerificationToken;
import com.landgo.userservice.entity.PasswordResetToken;
import com.landgo.userservice.entity.User;
import com.landgo.userservice.enums.AuthProvider;
import com.landgo.userservice.enums.Role;
import com.landgo.userservice.enums.UserType;
import com.landgo.userservice.exception.BadRequestException;
import com.landgo.userservice.exception.ConflictException;
import com.landgo.userservice.exception.ResourceNotFoundException;
import com.landgo.userservice.factory.OAuth2StrategyFactory;
import com.landgo.userservice.mapper.UserMapper;
import com.landgo.userservice.repository.EmailVerificationTokenRepository;
import com.landgo.userservice.repository.PasswordResetTokenRepository;
import com.landgo.userservice.repository.UserRepository;
import com.landgo.userservice.security.JwtTokenProvider;
import com.landgo.userservice.security.UserPrincipal;
import com.landgo.userservice.strategy.OAuth2AuthenticationStrategy;
import com.landgo.userservice.strategy.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final OAuth2StrategyFactory oAuth2StrategyFactory;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService;
    private final MfaService mfaService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int VERIFICATION_CODE_EXPIRY_MINUTES = 15;
    private static final int MAX_VERIFICATION_ATTEMPTS = 5;

    // ==========================================
    // REGISTER
    // ==========================================

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered", "AUTH_EMAIL_EXISTS");
        }

        Role requestedRole = mapRequestedRole(request.getRole());
        if (requestedRole == Role.AGENT) {
            if (request.getAgencyName() == null || request.getAgencyName().isBlank()) {
                throw new BadRequestException("Agency name is required for professional registration", "AUTH_AGENT_FIELDS_REQUIRED");
            }
            if (request.getLicenseNumber() == null || request.getLicenseNumber().isBlank()) {
                throw new BadRequestException("License number is required for professional registration", "AUTH_AGENT_FIELDS_REQUIRED");
            }
        }

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            if (request.getFullName() == null || request.getFullName().isBlank()) {
                request.setFullName(request.getFirstName() + " " + (request.getLastName() != null ? request.getLastName() : ""));
            }
        } else if (request.getFullName() != null && !request.getFullName().isBlank()) {
            String[] parts = request.getFullName().trim().split("\\s+", 2);
            request.setFirstName(parts[0]);
            request.setLastName(parts.length > 1 ? parts[1] : "");
        }

        User user = userMapper.toEntity(request);
        user.setAuthProvider(AuthProvider.EMAIL);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(requestedRole);
        user.setUserType(requestedRole == Role.AGENT ? UserType.AGENT : UserType.SELLER);
        user.setAgentAuthorizationAccepted(requestedRole == Role.AGENT);
        user.setRecoLicenseNumber(request.getLicenseNumber());

        user = userRepository.save(user);
        log.info("{} registered: {}", user.getRole(), user.getEmail());
        generateAndSendVerificationCode(user);
        return userMapper.toResponse(user);
    }

    // ==========================================
    // LOGIN
    // ==========================================

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        log.info("User login attempt: {}", user.getEmail());
        
        if (user.isMfaEnabled()) {
            log.info("MFA required for user: {}", user.getEmail());
            mfaService.initiateMfa(user);
            String mfaSession = tokenProvider.generateMfaToken(user.getId());
            return AuthResponse.builder()
                    .mfaRequired(true)
                    .mfaSession(mfaSession)
                    .user(userMapper.toResponse(user))
                    .build();
        }

        return generateAuthResponse(user);
    }

    // ==========================================
    // OAUTH2
    // ==========================================

    @Transactional
    public AuthResponse oauth2Login(OAuth2Request request) {
        OAuth2AuthenticationStrategy strategy = oAuth2StrategyFactory.getStrategy(request.getAuthProvider());
        OAuth2UserInfo userInfo = strategy.extractUserInfo(request.getToken());

        User user = userRepository.findByProviderIdAndAuthProvider(
                userInfo.getProviderId(), request.getAuthProvider())
                .orElseGet(() -> {
                    if (userRepository.existsByEmail(userInfo.getEmail()))
                        throw new ConflictException("Email already registered with different provider", "AUTH_EMAIL_EXISTS");
                    RegisterRequest rr = strategy.toRegisterRequest(userInfo);
                    User newUser = userMapper.toEntity(rr);
                    newUser.setRole(Role.SELLER);
                    newUser.setUserType(UserType.SELLER);
                    newUser.setAuthProvider(request.getAuthProvider());
                    newUser.setProviderId(userInfo.getProviderId());
                    newUser.setEmailVerified(true);
                    return userRepository.save(newUser);
                });

        log.info("OAuth2 login successful for: {}", user.getEmail());
        
        if (user.isMfaEnabled()) {
            log.info("MFA required for user: {}", user.getEmail());
            mfaService.initiateMfa(user);
            String mfaSession = tokenProvider.generateMfaToken(user.getId());
            return AuthResponse.builder()
                    .mfaRequired(true)
                    .mfaSession(mfaSession)
                    .user(userMapper.toResponse(user))
                    .build();
        }

        return generateAuthResponse(user);
    }

    // ==========================================
    // MFA VERIFICATION
    // ==========================================

    @Transactional(readOnly = true)
    public AuthResponse verifyMfa(MfaVerifyRequest request) {
        if (!tokenProvider.validateToken(request.getMfaSession())) {
            throw new BadRequestException("Invalid or expired MFA session", "MFA_SESSION_EXPIRED");
        }

        var claims = tokenProvider.getClaimsFromToken(request.getMfaSession());
        if (!"MFA_SESSION".equals(claims.get("type"))) {
            throw new BadRequestException("Invalid MFA session type", "MFA_SESSION_INVALID");
        }

        UUID userId = UUID.fromString(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!mfaService.verifyMfa(user, request.getCode())) {
            throw new BadRequestException("Invalid MFA code", "MFA_INVALID_CODE");
        }

        log.info("MFA verified successfully for user: {}", user.getEmail());
        return generateAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public void resendMfa(MfaResendRequest request) {
        if (!tokenProvider.validateToken(request.getMfaSession())) {
            throw new BadRequestException("Invalid or expired MFA session", "MFA_SESSION_EXPIRED");
        }

        var claims = tokenProvider.getClaimsFromToken(request.getMfaSession());
        if (!"MFA_SESSION".equals(claims.get("type"))) {
            throw new BadRequestException("Invalid MFA session type", "MFA_SESSION_INVALID");
        }

        UUID userId = UUID.fromString(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        log.info("Resending MFA code for user: {}", user.getEmail());
        mfaService.initiateMfa(user);
    }

    // ==========================================
    // LOGOUT
    // ==========================================

    public void logout(UserPrincipal userPrincipal) {
        log.info("User logged out: {}", userPrincipal.getId());
    }

    // ==========================================
    // CHANGE PASSWORD
    // ==========================================

    @Transactional
    public void changePassword(UserPrincipal userPrincipal, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword()))
            throw new BadRequestException("New password and confirm password do not match", "VALIDATION_ERROR");

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getAuthProvider() != AuthProvider.EMAIL)
            throw new BadRequestException("Cannot change password for " + user.getAuthProvider().name() + " accounts", "AUTH_OAUTH_PROVIDER_MISMATCH");

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword()))
            throw new BadRequestException("Current password is incorrect", "AUTH_WRONG_PASSWORD");

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getEmail());
    }

    // ==========================================
    // GET CURRENT USER
    // ==========================================

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toResponse(user);
    }

    // ==========================================
    // UPDATE PROFILE
    // ==========================================

    @Transactional
    public UserResponse updateProfile(UserPrincipal userPrincipal, UpdateProfileRequest request) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
            String[] parts = request.getFullName().trim().split("\\s+", 2);
            user.setFirstName(parts[0]);
            user.setLastName(parts.length > 1 ? parts[1] : "");
        }
        if (request.getPhone() != null) user.setPhone(request.getPhone().isBlank() ? null : request.getPhone());
        if (request.getProfileImageUrl() != null) user.setProfileImageUrl(request.getProfileImageUrl().isBlank() ? null : request.getProfileImageUrl());
        if (request.getLocation() != null) user.setLocation(request.getLocation().isBlank() ? null : request.getLocation());
        if (request.getProfessionalBio() != null) user.setProfessionalBio(request.getProfessionalBio().isBlank() ? null : request.getProfessionalBio());

        user = userRepository.save(user);
        log.info("Profile updated for user: {}", user.getEmail());
        return userMapper.toResponse(user);
    }

    // ==========================================
    // EMAIL VERIFICATION
    // ==========================================

    @Transactional
    public UserResponse verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email address"));

        if (user.isEmailVerified())
            throw new BadRequestException("Email is already verified", "AUTH_ALREADY_VERIFIED");

        EmailVerificationToken token = emailVerificationTokenRepository
                .findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new BadRequestException("No verification code found. Please request a new one.", "AUTH_INVALID_CODE"));

        if (token.isExpired())
            throw new BadRequestException("Verification code has expired. Please request a new one.", "AUTH_CODE_EXPIRED");
        if (token.getAttempts() >= MAX_VERIFICATION_ATTEMPTS)
            throw new BadRequestException("Too many failed attempts. Please request a new verification code.", "AUTH_TOO_MANY_ATTEMPTS");
        if (!token.getCode().equals(request.getCode())) {
            token.incrementAttempts();
            emailVerificationTokenRepository.save(token);
            int remaining = MAX_VERIFICATION_ATTEMPTS - token.getAttempts();
            throw new BadRequestException("Invalid verification code. " + remaining + " attempt(s) remaining.", "AUTH_INVALID_CODE");
        }

        token.setUsed(true);
        emailVerificationTokenRepository.save(token);
        user.setEmailVerified(true);
        user = userRepository.save(user);
        emailVerificationTokenRepository.invalidateAllTokensForUser(user);
        log.info("Email verified successfully for user: {}", user.getEmail());
        return userMapper.toResponse(user);
    }

    @Transactional
    public void resendVerificationCode(ResendVerificationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email address"));
        if (user.isEmailVerified())
            throw new BadRequestException("Email is already verified", "AUTH_ALREADY_VERIFIED");
        generateAndSendVerificationCode(user);
        log.info("Verification code resent to: {}", user.getEmail());
    }

    // ==========================================
    // FORGOT / RESET PASSWORD
    // ==========================================

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null || user.getAuthProvider() != AuthProvider.EMAIL) {
            log.info("Forgot password requested for non-existent or OAuth user: {}", request.getEmail());
            return;
        }
        passwordResetTokenRepository.invalidateAllTokensForUser(user);
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token).user(user).expiryDate(LocalDateTime.now().plusMinutes(30)).build();
        passwordResetTokenRepository.save(resetToken);
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), token);
        log.info("Password reset token generated for user: {}", user.getEmail());
    }

    @Transactional(readOnly = true)
    public void validateResetToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset link", "AUTH_INVALID_OR_EXPIRED_TOKEN"));
        if (resetToken.isExpired())
            throw new BadRequestException("Password reset link has expired. Please request a new one.", "AUTH_INVALID_OR_EXPIRED_TOKEN");
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and confirm password do not match", "VALIDATION_ERROR");
        }
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset link", "AUTH_INVALID_OR_EXPIRED_TOKEN"));
        if (resetToken.isExpired())
            throw new BadRequestException("Password reset link has expired. Please request a new one.", "AUTH_INVALID_OR_EXPIRED_TOKEN");

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        passwordResetTokenRepository.invalidateAllTokensForUser(user);
        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    // ==========================================
    // INTERNAL API — called by other services
    // ==========================================

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toResponse(user);
    }

    @Transactional
    public void updateUserRole(UUID userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setRole(role);
        userRepository.save(user);
        log.info("User {} role updated to {}", userId, role);
    }

    // ==========================================
    // HELPERS
    // ==========================================

    private void generateAndSendVerificationCode(User user) {
        emailVerificationTokenRepository.invalidateAllTokensForUser(user);
        String code = String.valueOf(SECURE_RANDOM.nextInt(9000) + 1000);
        EmailVerificationToken token = EmailVerificationToken.builder()
                .code(code).user(user).expiryDate(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES)).build();
        emailVerificationTokenRepository.save(token);
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), code);
    }

    private AuthResponse generateAuthResponse(User user) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String accessToken = tokenProvider.generateAccessToken(userPrincipal);
        String refreshToken = tokenProvider.generateRefreshToken(userPrincipal);
        return AuthResponse.builder()
                .accessToken(accessToken).refreshToken(refreshToken).tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpiration() / 1000)
                .user(userMapper.toResponse(user)).build();
    }

    private Role mapRequestedRole(String role) {
        if (role == null) {
            throw new BadRequestException("Role is required", "VALIDATION_ERROR");
        }
        return switch (role.trim().toLowerCase()) {
            case "buyer", "seller" -> Role.SELLER;
            case "professional" -> Role.AGENT;
            case "admin" -> Role.ADMIN;
            default -> throw new BadRequestException("Role must be one of: buyer, seller, professional, admin", "VALIDATION_ERROR");
        };
    }

    @Transactional
    public UserResponse updateMfaStatus(UserPrincipal userPrincipal, MfaSetupRequest request) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setMfaEnabled(request.isEnabled());
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone());
        }

        if (user.isMfaEnabled()) {
            // Trigger a verification to ensure the phone works
            mfaService.initiateMfa(user);
        }

        user = userRepository.save(user);
        log.info("MFA status updated for user: {}. Enabled: {}", user.getEmail(), user.isMfaEnabled());
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }
}


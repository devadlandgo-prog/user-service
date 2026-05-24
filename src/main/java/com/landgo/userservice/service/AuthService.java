package com.landgo.userservice.service;

import com.landgo.userservice.dto.request.*;
import com.landgo.userservice.dto.response.AuthResponse;
import com.landgo.userservice.dto.response.UserResponse;
import com.landgo.userservice.entity.EmailVerificationToken;
import com.landgo.userservice.entity.PasswordResetToken;
import com.landgo.userservice.entity.User;
import com.landgo.userservice.entity.VendorCertification;
import com.landgo.userservice.enums.AuthProvider;
import com.landgo.userservice.enums.Role;
import com.landgo.userservice.enums.UserType;
import com.landgo.userservice.exception.BadRequestException;
import com.landgo.userservice.exception.ConflictException;
import com.landgo.userservice.exception.ApiException;
import com.landgo.userservice.exception.ResourceNotFoundException;
import com.landgo.userservice.factory.OAuth2StrategyFactory;
import com.landgo.userservice.mapper.UserMapper;
import com.landgo.userservice.repository.EmailVerificationTokenRepository;
import com.landgo.userservice.repository.PasswordResetTokenRepository;
import com.landgo.userservice.repository.UserRepository;
import com.landgo.userservice.repository.VendorProfileRepository;
import com.landgo.userservice.security.JwtTokenProvider;
import com.landgo.userservice.security.UserPrincipal;
import com.landgo.userservice.strategy.OAuth2AuthenticationStrategy;
import com.landgo.userservice.strategy.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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
    private final LoginAuditService loginAuditService;
    private final VendorProfileRepository vendorProfileRepository;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int VERIFICATION_CODE_EXPIRY_MINUTES = 15;
    private static final int MAX_VERIFICATION_ATTEMPTS = 5;

    // ==========================================
    // REGISTER
    // ==========================================

    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email {} already exists", request.getEmail());
            throw new ConflictException("Email already registered", "AUTH_EMAIL_EXISTS");
        }

        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            log.warn("Registration failed: Phone {} already exists", request.getPhone());
            throw new ConflictException("Phone number already registered", "AUTH_PHONE_EXISTS");
        }

        Role requestedRole = mapRequestedRole(request.getRole());
        boolean isProfessional = isProfessionalRole(request.getRole());

        if (isProfessional) {
            if (request.getAgencyName() == null || request.getAgencyName().isBlank()) {
                throw new BadRequestException("Agency name is required for professional registration", "AUTH_AGENT_FIELDS_REQUIRED");
            }
            if (request.getLicenseNumber() == null || request.getLicenseNumber().isBlank()) {
                throw new BadRequestException("License number is required for professional registration", "AUTH_AGENT_FIELDS_REQUIRED");
            }
        }

        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new BadRequestException("Full name is required", "VALIDATION_ERROR");
        }

        String[] parts = request.getFullName().trim().split("\\s+", 2);
        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[1] : "";
        User user = userMapper.toEntity(request);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAuthProvider(AuthProvider.EMAIL);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(requestedRole);
        user.setUserType(mapUserType(request.getRole()));
        user.setProfessional(isProfessional);
        user.setAgentAuthorizationAccepted(isProfessional);
        user.setRecoLicenseNumber(request.getLicenseNumber());

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getId());
        generateAndSendVerificationCode(user);
        log.debug("Verification email process initiated for: {}", user.getEmail());
        return userMapper.toResponse(user);
    }

    // ==========================================
    // LOGIN
    // ==========================================

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for: {}", request.getEmailOrPhone());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmailOrPhone(), request.getPassword()));
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
        if (request.getPlatform() != null && !request.getPlatform().isBlank()) {
            log.info("OAuth2 login requested via {} from platform {}", request.getAuthProvider(), request.getPlatform());
        } else {
            log.info("OAuth2 login requested via {}", request.getAuthProvider());
        }
        OAuth2AuthenticationStrategy strategy = oAuth2StrategyFactory.getStrategy(request.getAuthProvider());
        OAuth2UserInfo userInfo = strategy.extractUserInfo(request.getToken());

        User user = userRepository.findByProviderIdAndAuthProvider(
                userInfo.getProviderId(), request.getAuthProvider())
                .orElseGet(() -> {
                    if (userRepository.existsByEmail(userInfo.getEmail()))
                        throw new ConflictException("Email already registered with different provider", "AUTH_EMAIL_EXISTS");
                    RegisterRequest rr = strategy.toRegisterRequest(userInfo);
                    User newUser = userMapper.toEntity(rr);
                    newUser.setRole(Role.VENDOR);
                    newUser.setUserType(UserType.SELLER);
                    newUser.setProfessional(false);
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
        return toUserResponseWithProfessionalProfile(user);
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
        if (request.getTimezone() != null) user.setTimezone(request.getTimezone().isBlank() ? null : request.getTimezone());

        if (user.isProfessional()) {
            if (request.getCompanyName() != null) user.setAgencyName(request.getCompanyName());
            if (request.getLicenseNumber() != null) user.setRecoLicenseNumber(request.getLicenseNumber());
        }

        user = userRepository.save(user);

        if (user.isProfessional()) {
            var profile = vendorProfileRepository.findByUser(user);
            if (profile.isEmpty()) {
                var newProfile = new com.landgo.userservice.entity.VendorProfile();
                newProfile.setUser(user);
                newProfile.setBusinessAddress("TBD");
                newProfile.setBusinessCity("TBD");
                newProfile.setBusinessState("TBD");
                newProfile.setBusinessZipCode("TBD");
                newProfile.setBusinessCountry("TBD");
                profile = java.util.Optional.of(vendorProfileRepository.save(newProfile));
            }
            var profileEntity = profile.get();
            if (request.getCompanyName() != null) profileEntity.setCompanyName(request.getCompanyName());
            if (request.getLicenseNumber() != null) profileEntity.setBusinessLicense(request.getLicenseNumber());
            if (request.getSpecialization() != null) profileEntity.setSpecialization(request.getSpecialization());
            if (request.getYearsOfExperience() != null) profileEntity.setYearsOfExperience(request.getYearsOfExperience());
            if (request.getServiceArea() != null) profileEntity.setServiceArea(request.getServiceArea());
            if (request.getCertifications() != null) profileEntity.setCertifications(mapCertifications(request.getCertifications()));
            if (request.getBio() != null) profileEntity.setBio(request.getBio());
            if (request.getCompanyDescription() != null) profileEntity.setCompanyDescription(request.getCompanyDescription());
            if (request.getBusinessAddress() != null) profileEntity.setBusinessAddress(request.getBusinessAddress());
            if (request.getBusinessCity() != null) profileEntity.setBusinessCity(request.getBusinessCity());
            if (request.getBusinessState() != null) profileEntity.setBusinessState(request.getBusinessState());
            if (request.getBusinessZipCode() != null) profileEntity.setBusinessZipCode(request.getBusinessZipCode());
            if (request.getBusinessCountry() != null) profileEntity.setBusinessCountry(request.getBusinessCountry());
            if (request.getWebsite() != null) profileEntity.setWebsite(request.getWebsite());
            if (request.getPhone() != null) profileEntity.setPhoneNumber(request.getPhone().isBlank() ? null : request.getPhone());
            if (request.getCompanyLogo() != null)
                profileEntity.setCompanyLogo(request.getCompanyLogo().isBlank() ? null : request.getCompanyLogo());
            vendorProfileRepository.save(profileEntity);
        }

        log.info("Profile updated for user: {}", user.getEmail());
        return toUserResponseWithProfessionalProfile(user);
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

        return completeEmailVerification(token);
    }

    @Transactional
    public UserResponse verifyEmailByToken(String tokenValue) {
        UUID tokenId;
        try {
            tokenId = UUID.fromString(tokenValue);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid verification link", "AUTH_INVALID_CODE");
        }

        EmailVerificationToken token = emailVerificationTokenRepository.findById(tokenId)
                .orElseThrow(() -> new BadRequestException("Invalid verification link", "AUTH_INVALID_CODE"));

        if (token.isUsed()) {
            throw new BadRequestException("Verification link is no longer valid. Please request a new one.", "AUTH_INVALID_CODE");
        }
        if (token.isExpired()) {
            throw new BadRequestException("Verification link has expired. Please request a new one.", "AUTH_CODE_EXPIRED");
        }

        return completeEmailVerification(token);
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

    @Transactional(readOnly = true)
    public AuthResponse refreshAccessToken(RefreshTokenRequest request) {
        if (!tokenProvider.validateToken(request.getRefreshToken())) {
            throw new ApiException("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED, "AUTH_INVALID_REFRESH_TOKEN");
        }

        var claims = tokenProvider.getClaimsFromToken(request.getRefreshToken());
        Object tokenType = claims.get("type");
        if (!"REFRESH".equals(tokenType)) {
            throw new ApiException("Invalid token type for refresh", HttpStatus.UNAUTHORIZED, "AUTH_INVALID_REFRESH_TOKEN");
        }

        UUID userId = UUID.fromString(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.isActive()) {
            throw new ApiException("User account is inactive", HttpStatus.UNAUTHORIZED, "AUTH_ACCOUNT_INACTIVE");
        }

        return generateAuthResponse(user);
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
    public String getLatestVerificationCode(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return emailVerificationTokenRepository
                .findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .map(EmailVerificationToken::getCode)
                .orElseThrow(() -> new ResourceNotFoundException("No active verification code found for user"));
    }

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

    @Transactional
    public void deleteAccount(UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(false);
        user.setMfaEnabled(false);
        user.setMfaVerified(false);
        userRepository.save(user);
        log.info("Account deactivated for user: {}", user.getEmail());
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
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), code, token.getId().toString());
    }

    private UserResponse completeEmailVerification(EmailVerificationToken token) {
        User user = token.getUser();
        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified", "AUTH_ALREADY_VERIFIED");
        }

        token.setUsed(true);
        emailVerificationTokenRepository.save(token);
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        user = userRepository.save(user);
        emailVerificationTokenRepository.invalidateAllTokensForUser(user);
        log.info("Email verified successfully for user: {}", user.getEmail());
        return userMapper.toResponse(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        loginAuditService.recordLastLogin(user.getId());
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
            case "buyer", "seller", "vendor", "professional", "agent" -> Role.VENDOR;
            case "admin" -> Role.ADMIN;
            default -> throw new BadRequestException("Role must be one of: buyer, seller, vendor, agent, professional, admin", "VALIDATION_ERROR");
        };
    }

    private UserType mapUserType(String role) {
        if (role == null) return UserType.SELLER;
        return switch (role.trim().toLowerCase()) {
            case "buyer" -> UserType.BUYER;
            default -> UserType.SELLER;
        };
    }

    private boolean isProfessionalRole(String role) {
        if (role == null) return false;
        return switch (role.trim().toLowerCase()) {
            case "professional", "agent" -> true;
            default -> false;
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

    // ==========================================
    // ADMIN — PROFESSIONALS
    // ==========================================

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllProfessionals(Pageable pageable) {
        return userRepository.findByIsProfessionalTrue(pageable).map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> searchProfessionals(String query, Pageable pageable) {
        return userRepository.searchProfessionals(query, pageable).map(userMapper::toResponse);
    }

    @Transactional
    public UserResponse updateProfessionalProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Update User Entity fields
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
        if (request.getTimezone() != null) user.setTimezone(request.getTimezone().isBlank() ? null : request.getTimezone());
        
        // Sync redundant fields on User entity if it's a professional
        if (user.isProfessional()) {
            if (request.getCompanyName() != null) user.setAgencyName(request.getCompanyName());
            if (request.getLicenseNumber() != null) user.setRecoLicenseNumber(request.getLicenseNumber());
        }

        user = userRepository.save(user);

        // Update VendorProfile entity if it exists
        if (user.isProfessional()) {
            vendorProfileRepository.findById(userId).ifPresent(profile -> {
                if (request.getCompanyName() != null) profile.setCompanyName(request.getCompanyName());
                if (request.getLicenseNumber() != null) profile.setBusinessLicense(request.getLicenseNumber());
                if (request.getSpecialization() != null) profile.setSpecialization(request.getSpecialization());
                if (request.getYearsOfExperience() != null) profile.setYearsOfExperience(request.getYearsOfExperience());
                if (request.getServiceArea() != null) profile.setServiceArea(request.getServiceArea());
                if (request.getCertifications() != null) profile.setCertifications(mapCertifications(request.getCertifications()));
                if (request.getBio() != null) profile.setBio(request.getBio());
                if (request.getCompanyDescription() != null) profile.setCompanyDescription(request.getCompanyDescription());
                
                // Address fields
                if (request.getBusinessAddress() != null) profile.setBusinessAddress(request.getBusinessAddress());
                if (request.getBusinessCity() != null) profile.setBusinessCity(request.getBusinessCity());
                if (request.getBusinessState() != null) profile.setBusinessState(request.getBusinessState());
                if (request.getBusinessZipCode() != null) profile.setBusinessZipCode(request.getBusinessZipCode());
                if (request.getBusinessCountry() != null) profile.setBusinessCountry(request.getBusinessCountry());
                if (request.getWebsite() != null) profile.setWebsite(request.getWebsite());
                if (request.getCompanyLogo() != null)
                    profile.setCompanyLogo(request.getCompanyLogo().isBlank() ? null : request.getCompanyLogo());
                
                vendorProfileRepository.save(profile);
            });
        }

        log.info("Admin updated professional profile for user: {}", userId);
        return toUserResponseWithProfessionalProfile(user);
    }

    private UserResponse toUserResponseWithProfessionalProfile(User user) {
        UserResponse response = userMapper.toResponse(user);
        if (user.isProfessional()) {
            vendorProfileRepository.findByUser(user)
                    .ifPresent(profile -> {
                        response.setCompanyLogo(profile.getCompanyLogo());
                        response.setSpecialization(profile.getSpecialization());
                        response.setYearsOfExperience(profile.getYearsOfExperience());
                        response.setServiceArea(profile.getServiceArea());
                    });
        }
        return response;
    }

    @Transactional
    public void deactivateProfessional(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(false);
        user.setProfessional(false);
        userRepository.save(user);
        log.info("Admin deactivated professional user: {}", userId);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
        log.info("Admin performed hard delete of user: {}", userId);
    }

    private List<VendorCertification> mapCertifications(List<CertificationRequest> certifications) {
        return certifications.stream()
                .map(certification -> VendorCertification.builder()
                        .title(certification.getTitle())
                        .fileKey(certification.getFileKey())
                        .build())
                .collect(Collectors.toList());
    }
}

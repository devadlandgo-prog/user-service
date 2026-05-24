package com.landgo.userservice.service;

import com.landgo.userservice.dto.request.ProfessionalRegisterRequest;
import com.landgo.userservice.dto.request.CertificationRequest;
import com.landgo.userservice.dto.request.VendorProfileRequest;
import com.landgo.userservice.dto.response.VendorResponse;
import com.landgo.userservice.entity.User;
import com.landgo.userservice.entity.VendorCertification;
import com.landgo.userservice.entity.VendorProfile;
import com.landgo.userservice.enums.AuthProvider;
import com.landgo.userservice.enums.Role;
import com.landgo.userservice.enums.UserType;
import com.landgo.userservice.exception.BadRequestException;
import com.landgo.userservice.exception.ConflictException;
import com.landgo.userservice.exception.ResourceNotFoundException;
import com.landgo.userservice.mapper.VendorProfileMapper;
import com.landgo.userservice.repository.UserRepository;
import com.landgo.userservice.repository.VendorProfileRepository;
import com.landgo.userservice.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorProfileRepository vendorProfileRepository;
    private final UserRepository userRepository;
    private final VendorProfileMapper vendorProfileMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void incrementViewCount(UUID profileId) {
        vendorProfileRepository.incrementViewCount(profileId);
        log.debug("Incremented view count for profile: {}", profileId);
    }

    @Transactional
    public VendorResponse registerProfessional(ProfessionalRegisterRequest request, UserPrincipal userPrincipal) {
        log.info("Starting combined professional registration for email: {}", request.getEmail());

        if (userPrincipal != null) {
            User user = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

            if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
                throw new BadRequestException("Email does not match authenticated user", "VALIDATION_ERROR");
            }

            if (vendorProfileRepository.findByUser(user).isPresent()) {
                throw new BadRequestException("Vendor profile already exists for this user", "PROFESSIONAL_PROFILE_EXISTS");
            }

            VendorProfile profile = vendorProfileMapper.toEntity(request);
            profile.setId(user.getId());
            profile.setUser(user);
            if (!StringUtils.hasText(profile.getPhoneNumber())) {
                profile.setPhoneNumber(user.getPhone());
            }

            user.setRole(Role.VENDOR);
            user.setUserType(UserType.SELLER);
            user.setProfessional(true);
            user.setAgentAuthorizationAccepted(true);
            user.setAgencyName(request.getCompanyName());
            user.setRecoLicenseNumber(request.getLicenseNumber());
            userRepository.save(user);

            VendorProfile saved = vendorProfileRepository.save(profile);
            log.info("Professional profile created for existing user: {}", user.getId());
            return vendorProfileMapper.toResponse(saved);
        }

        if (!StringUtils.hasText(request.getEmail())) {
            throw new BadRequestException("Email is required", "VALIDATION_ERROR");
        }
        if (!StringUtils.hasText(request.getFullName())) {
            throw new BadRequestException("Full name is required", "VALIDATION_ERROR");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new BadRequestException("Password is required", "VALIDATION_ERROR");
        }
        if (!StringUtils.hasText(request.getPhone())) {
            throw new BadRequestException("Phone is required", "VALIDATION_ERROR");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered", "AUTH_EMAIL_EXISTS");
        }

        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new ConflictException("Phone number already registered", "AUTH_PHONE_EXISTS");
        }

        // 1. Create and Save User
        String[] parts = request.getFullName().trim().split("\\s+", 2);
        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[1] : "";

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .firstName(firstName)
                .lastName(lastName)
                .phone(request.getPhone())
                .role(Role.VENDOR)
                .userType(UserType.SELLER)
                .isProfessional(true)
                .agentAuthorizationAccepted(true)
                .agencyName(request.getCompanyName())
                .recoLicenseNumber(request.getLicenseNumber())
                .authProvider(AuthProvider.EMAIL)
                .active(true)
                .build();

        user = userRepository.save(user);
        log.info("User created with ID: {}", user.getId());

        // 2. Create and Save VendorProfile
        VendorProfile profile = vendorProfileMapper.toEntity(request);
        profile.setId(user.getId());
        profile.setUser(user);
        
        VendorProfile saved = vendorProfileRepository.save(profile);
        log.info("Professional profile created for user: {}", user.getId());

        return vendorProfileMapper.toResponse(saved);
    }

    @Transactional
    public VendorResponse createVendorProfile(@NonNull UUID userId, VendorProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (vendorProfileRepository.findByUser(user).isPresent()) {
            throw new BadRequestException("Vendor profile already exists for this user");
        }

        VendorProfile profile = vendorProfileMapper.toEntity(request);
        profile.setId(userId); // Use the user's ID for the vendor profile (shared primary key)
        profile.setUser(user);
        user.setRole(com.landgo.userservice.enums.Role.VENDOR);
        userRepository.save(user);

        VendorProfile saved = vendorProfileRepository.save(profile);
        log.info("Vendor profile created for user: {}", userId);
        return vendorProfileMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public VendorResponse getVendorProfile(@NonNull UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        VendorProfile profile = vendorProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "userId", userId));
        return vendorProfileMapper.toResponse(profile);
    }

    @Transactional(readOnly = true)
    public VendorResponse getVendorProfileResolved(@NonNull UUID identifier) {
        return vendorProfileRepository.findById(identifier)
                .map(vendorProfileMapper::toResponse)
                .or(() -> userRepository.findById(identifier)
                        .flatMap(vendorProfileRepository::findByUser)
                        .map(vendorProfileMapper::toResponse))
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "id", identifier));
    }

    @Transactional
    public VendorResponse updateVendorProfile(@NonNull UUID userId, VendorProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        VendorProfile profile = vendorProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "userId", userId));

        profile.setCompanyName(request.getCompanyName());
        profile.setCompanyDescription(request.getCompanyDescription());
        profile.setCompanyLogo(request.getCompanyLogo());
        profile.setBusinessLicense(request.getBusinessLicense());
        profile.setBusinessAddress(request.getBusinessAddress());
        profile.setBusinessCity(request.getBusinessCity());
        profile.setBusinessState(request.getBusinessState());
        profile.setBusinessZipCode(request.getBusinessZipCode());
        profile.setBusinessCountry(request.getBusinessCountry());
        profile.setWebsite(request.getWebsite());
        profile.setPhoneNumber(request.getPhoneNumber());
        if (request.getCertifications() != null) {
            profile.setCertifications(mapCertifications(request.getCertifications()));
        }

        VendorProfile updated = vendorProfileRepository.save(profile);
        log.info("Vendor profile updated for user: {}", userId);
        return vendorProfileMapper.toResponse(updated);
    }

    @Transactional(readOnly = true)
    public Map<UUID, VendorResponse> getVendorProfilesBatch(List<UUID> userIds) {
        return vendorProfileRepository.findAllByIdIn(userIds).stream()
                .collect(Collectors.toMap(
                        vp -> vp.getUser().getId(),
                        vendorProfileMapper::toResponse
                ));
    }

    @Transactional
    public VendorResponse getVendorProfileById(@NonNull UUID vendorId) {
        VendorProfile profile = vendorProfileRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "id", vendorId));
        return vendorProfileMapper.toResponse(profile);
    }

    @Transactional
    public VendorResponse verifyProfessional(UUID userId, com.landgo.userservice.enums.VerificationStatus status, String notes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        VendorProfile profile = vendorProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "userId", userId));

        profile.setVerificationStatus(status);
        profile.setVerificationNotes(notes);
        
        if (status == com.landgo.userservice.enums.VerificationStatus.APPROVED) {
            profile.setVerified(true);
        } else {
            profile.setVerified(false);
        }

        VendorProfile saved = vendorProfileRepository.save(profile);
        log.info("Professional verification updated for user: {} to {}", userId, status);
        return vendorProfileMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<VendorResponse> getVerifiedProfessionals(String specialization, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Pageable pageOnly = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize());
        String sortKey = pageable.getSort().stream()
                .findFirst()
                .map(org.springframework.data.domain.Sort.Order::getProperty)
                .orElse("createdAt");

        if (specialization == null || specialization.isBlank()) {
            return vendorProfileRepository.findByVerifiedTrue(pageable)
                    .map(vendorProfileMapper::toResponse);
        }
        org.springframework.data.domain.Page<VendorProfile> page = switch (sortKey) {
            case "rating" -> vendorProfileRepository.findByVerifiedTrueAndSpecializationOrderByRatingDesc(specialization, pageOnly);
            case "totalReviews" -> vendorProfileRepository.findByVerifiedTrueAndSpecializationOrderByTotalReviewsDesc(specialization, pageOnly);
            case "yearsOfExperience" -> vendorProfileRepository.findByVerifiedTrueAndSpecializationOrderByYearsOfExperienceDesc(specialization, pageOnly);
            default -> vendorProfileRepository.findByVerifiedTrueAndSpecializationOrderByCreatedAtDesc(specialization, pageOnly);
        };
        return page
                .map(vendorProfileMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<VendorResponse> searchProfessionals(String q, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Pageable pageOnly = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize());
        String sortKey = pageable.getSort().stream()
                .findFirst()
                .map(org.springframework.data.domain.Sort.Order::getProperty)
                .orElse("createdAt");

        org.springframework.data.domain.Page<VendorProfile> page = switch (sortKey) {
            case "rating" -> vendorProfileRepository.searchProfessionalsOrderByRatingDesc(q, pageOnly);
            case "totalReviews" -> vendorProfileRepository.searchProfessionalsOrderByTotalReviewsDesc(q, pageOnly);
            case "yearsOfExperience" -> vendorProfileRepository.searchProfessionalsOrderByYearsOfExperienceDesc(q, pageOnly);
            default -> vendorProfileRepository.searchProfessionalsOrderByCreatedAtDesc(q, pageOnly);
        };

        return page
                .map(vendorProfileMapper::toResponse);
    }

    private List<VendorCertification> mapCertifications(List<CertificationRequest> certifications) {
        return certifications.stream()
                .map(certification -> VendorCertification.builder()
                        .title(certification.getTitle())
                        .fileKey(certification.getFileKey())
                        .build())
                .toList();
    }
}

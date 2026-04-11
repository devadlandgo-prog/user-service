package com.landgo.userservice.service;

import com.landgo.userservice.dto.request.VendorProfileRequest;
import com.landgo.userservice.dto.response.VendorResponse;
import com.landgo.userservice.entity.User;
import com.landgo.userservice.entity.VendorProfile;
import com.landgo.userservice.exception.BadRequestException;
import com.landgo.userservice.exception.ResourceNotFoundException;
import com.landgo.userservice.mapper.VendorProfileMapper;
import com.landgo.userservice.repository.UserRepository;
import com.landgo.userservice.repository.VendorProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorProfileRepository vendorProfileRepository;
    private final UserRepository userRepository;
    private final VendorProfileMapper vendorProfileMapper;

    @Transactional
    public VendorResponse createVendorProfile(@NonNull UUID userId, VendorProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (vendorProfileRepository.findByUser(user).isPresent()) {
            throw new BadRequestException("Vendor profile already exists for this user");
        }

        VendorProfile profile = vendorProfileMapper.toEntity(request);
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

        VendorProfile updated = vendorProfileRepository.save(profile);
        log.info("Vendor profile updated for user: {}", userId);
        return vendorProfileMapper.toResponse(updated);
    }

    @Transactional(readOnly = true)
    public VendorResponse getVendorProfileById(@NonNull UUID vendorId) {
        VendorProfile profile = vendorProfileRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "id", vendorId));
        return vendorProfileMapper.toResponse(profile);
    }
}

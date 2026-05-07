package com.landgo.userservice.service;

import com.landgo.userservice.dto.client.CoreVendorListingStatsDto;
import com.landgo.userservice.dto.response.UserStatsResponse;
import com.landgo.userservice.entity.User;
import com.landgo.userservice.exception.ResourceNotFoundException;
import com.landgo.userservice.repository.UserRepository;
import com.landgo.userservice.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class UserStatsService {

    private final UserRepository userRepository;
    private final CoreServiceClient coreServiceClient;
    private final PaymentServiceClient paymentServiceClient;

    @Transactional(readOnly = true)
    public UserStatsResponse buildStats(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CoreVendorListingStatsDto listing = coreServiceClient.fetchVendorListingStats(user.getId());
        long activeSubscriptions = paymentServiceClient.countActiveSubscriptions(user.getId());

        LocalDateTime lastLoginDisplay = user.getLastLoginAt() != null
                ? user.getLastLoginAt()
                : user.getUpdatedAt();

        LocalDateTime created = user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now();
        long memberSinceMonths = Math.max(0, ChronoUnit.MONTHS.between(created, LocalDateTime.now()));

        return UserStatsResponse.builder()
                .activeListings(listing.getActiveListings())
                .totalListings(listing.getTotalListings())
                .totalViews(listing.getTotalViews())
                .totalEnquiries(listing.getTotalEnquiries())
                .activeSubscriptions(activeSubscriptions)
                .profileCompleteness(computeProfileCompleteness(user))
                .lastLoginAt(lastLoginDisplay)
                .memberSinceMonths(memberSinceMonths)
                .build();
    }

    static int computeProfileCompleteness(User user) {
        int score = 0;
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            score += 20;
        }
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isBlank()) {
            score += 20;
        }
        if (user.getLocation() != null && !user.getLocation().isBlank()) {
            score += 20;
        }
        if (user.isEmailVerified()) {
            score += 15;
        }
        String name = user.getFullName();
        if (name != null && !name.isBlank()) {
            score += 10;
        }
        if (user.getProfessionalBio() != null && !user.getProfessionalBio().isBlank()) {
            score += 15;
        }
        return Math.min(100, score);
    }
}

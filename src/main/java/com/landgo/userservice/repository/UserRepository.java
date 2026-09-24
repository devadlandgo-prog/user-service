package com.landgo.userservice.repository;

import com.landgo.userservice.entity.User;
import com.landgo.userservice.enums.AuthProvider;
import com.landgo.userservice.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.email = :identifier OR u.phone = :identifier")
    Optional<User> findByIdentifier(@Param("identifier") String identifier);

    Optional<User> findByProviderIdAndAuthProvider(String providerId, AuthProvider authProvider);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    long countByRole(Role role);
    Page<User> findByIsProfessionalTrue(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.isProfessional = true AND u.active = true AND (" +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(u.agencyName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(u.professionalBio) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(u.location) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<User> searchProfessionals(@Param("q") String query, Pageable pageable);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :ts WHERE u.id = :id")
    void updateLastLoginAt(@Param("id") UUID id, @Param("ts") LocalDateTime ts);

    /**
     * Subscribers whose paid-through date falls exactly {@code days} from today.
     *
     * <p>Excludes land_listing rows: those are one-time credit purchases whose end_date exists
     * only because the column is NOT NULL. They never expire, so warning about them would be
     * false. Also excludes subscriptions already set to renew — a renewing plan is not expiring.
     */
    @Query(value = "SELECT u.email as email, u.full_name as fullName, s.end_date as endDate, s.plan_category as planCategory " +
           "FROM users.users u " +
           "JOIN payments.subscriptions s ON u.id = s.user_id " +
           "WHERE s.status = 'ACTIVE' " +
           "AND s.auto_renew = false " +
           "AND LOWER(COALESCE(s.plan_category, '')) <> 'land_listing' " +
           "AND CAST(s.end_date AS DATE) = CURRENT_DATE + :days", nativeQuery = true)
    java.util.List<com.landgo.userservice.dto.ExpiringSubscriptionProjection> findUsersWithExpiringSubscriptions(@Param("days") int days);
}

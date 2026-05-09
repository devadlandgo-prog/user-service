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
    Optional<User> findByProviderIdAndAuthProvider(String providerId, AuthProvider authProvider);
    boolean existsByEmail(String email);
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
}

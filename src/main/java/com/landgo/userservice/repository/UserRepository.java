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

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderIdAndAuthProvider(String providerId, AuthProvider authProvider);
    boolean existsByEmail(String email);
    long countByRole(Role role);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :ts WHERE u.id = :id")
    void updateLastLoginAt(@Param("id") UUID id, @Param("ts") LocalDateTime ts);
}

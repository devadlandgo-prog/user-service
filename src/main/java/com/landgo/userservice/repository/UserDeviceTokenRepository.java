package com.landgo.userservice.repository;

import com.landgo.userservice.entity.UserDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, UUID> {
    Optional<UserDeviceToken> findByFcmToken(String fcmToken);

    List<UserDeviceToken> findByUserIdAndActiveTrue(UUID userId);

    @Modifying
    @Query("UPDATE UserDeviceToken t SET t.active = false WHERE t.fcmToken IN :tokens")
    void deactivateTokens(@Param("tokens") List<String> tokens);

    @Query("SELECT t.fcmToken FROM UserDeviceToken t WHERE t.active = true")
    List<String> findAllActiveTokens();

    @Query("SELECT t.fcmToken FROM UserDeviceToken t JOIN User u ON t.userId = u.id WHERE t.active = true AND u.userType = 'BUYER'")
    List<String> findActiveTokensForBuyers();

    @Query("SELECT t.fcmToken FROM UserDeviceToken t JOIN User u ON t.userId = u.id WHERE t.active = true AND u.userType = 'SELLER'")
    List<String> findActiveTokensForSellers();
    
    @Query("SELECT t.fcmToken FROM UserDeviceToken t JOIN User u ON t.userId = u.id WHERE t.active = true AND u.role = 'VENDOR'")
    List<String> findActiveTokensForVendors();
}

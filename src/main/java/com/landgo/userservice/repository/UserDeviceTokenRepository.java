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

    // Audience queries return entities rather than bare token strings because campaign delivery
    // needs the owning user id to write per-recipient delivery logs.

    Optional<UserDeviceToken> findByFcmTokenAndActiveTrue(String fcmToken);

    @Query("SELECT t FROM UserDeviceToken t WHERE t.active = true")
    List<UserDeviceToken> findAllActiveDevices();

    @Query("SELECT t FROM UserDeviceToken t WHERE t.active = true AND upper(t.platform) IN :platforms")
    List<UserDeviceToken> findActiveDevicesByPlatforms(@Param("platforms") List<String> platforms);

    @Query("SELECT t FROM UserDeviceToken t JOIN User u ON t.userId = u.id WHERE t.active = true AND u.userType = 'BUYER'")
    List<UserDeviceToken> findActiveDevicesForBuyers();

    @Query("SELECT t FROM UserDeviceToken t JOIN User u ON t.userId = u.id WHERE t.active = true AND u.userType = 'SELLER'")
    List<UserDeviceToken> findActiveDevicesForSellers();

    @Query("SELECT t FROM UserDeviceToken t JOIN User u ON t.userId = u.id WHERE t.active = true AND u.role = 'VENDOR'")
    List<UserDeviceToken> findActiveDevicesForVendors();

    @Query("SELECT t FROM UserDeviceToken t WHERE t.active = true AND t.userId = :userId")
    List<UserDeviceToken> findActiveDevicesForUser(@Param("userId") UUID userId);
}

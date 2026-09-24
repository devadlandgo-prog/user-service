package com.landgo.userservice.repository;

import com.landgo.userservice.entity.EmailDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailDeliveryRepository extends JpaRepository<EmailDelivery, UUID> {
    Optional<EmailDelivery> findByIdempotencyKey(String idempotencyKey);
}

package com.landgo.userservice.repository;

import com.landgo.userservice.entity.PushDeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PushDeliveryLogRepository extends JpaRepository<PushDeliveryLog, UUID> {
}

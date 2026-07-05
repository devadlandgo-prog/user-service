package com.landgo.userservice.repository;

import com.landgo.userservice.entity.PushCampaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PushCampaignRepository extends JpaRepository<PushCampaign, UUID> {
    Page<PushCampaign> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<PushCampaign> findByStatus(String status);
}

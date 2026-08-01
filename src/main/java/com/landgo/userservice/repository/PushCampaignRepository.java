package com.landgo.userservice.repository;

import com.landgo.userservice.entity.PushCampaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PushCampaignRepository extends JpaRepository<PushCampaign, UUID> {
    Page<PushCampaign> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<PushCampaign> findByStatus(String status);

    /**
     * Campaigns ready to be dispatched: queued for immediate send, or scheduled for a time that
     * has now passed.
     */
    @Query("""
            SELECT c FROM PushCampaign c
            WHERE c.status IN ('QUEUED', 'SCHEDULED')
              AND (c.scheduledAt IS NULL OR c.scheduledAt <= :now)
            ORDER BY c.createdAt ASC
            """)
    List<PushCampaign> findDispatchable(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * Campaigns a worker claimed but never finished — the process died mid-send, or the
     * scheduler was disabled while a batch was in flight.
     */
    @Query("""
            SELECT c FROM PushCampaign c
            WHERE c.status = 'PROCESSING'
              AND c.startedAt IS NOT NULL
              AND c.startedAt < :cutoff
            """)
    List<PushCampaign> findStaleProcessing(@Param("cutoff") LocalDateTime cutoff);
}

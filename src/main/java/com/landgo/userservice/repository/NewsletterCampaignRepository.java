package com.landgo.userservice.repository;

import com.landgo.userservice.entity.NewsletterCampaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NewsletterCampaignRepository extends JpaRepository<NewsletterCampaign, UUID> {

    Page<NewsletterCampaign> findAllByDeletedFalse(Pageable pageable);

    /** Campaigns accepted by the publish endpoint and waiting for the delivery worker. */
    @Query("""
            SELECT c FROM NewsletterCampaign c
            WHERE c.status = 'QUEUED'
            ORDER BY c.createdAt ASC
            """)
    List<NewsletterCampaign> findDispatchable(Pageable pageable);

    /** Campaigns a worker claimed but never finished, e.g. the process died mid-broadcast. */
    @Query("""
            SELECT c FROM NewsletterCampaign c
            WHERE c.status = 'PROCESSING'
              AND c.startedAt IS NOT NULL
              AND c.startedAt < :cutoff
            """)
    List<NewsletterCampaign> findStaleProcessing(@Param("cutoff") LocalDateTime cutoff);
}

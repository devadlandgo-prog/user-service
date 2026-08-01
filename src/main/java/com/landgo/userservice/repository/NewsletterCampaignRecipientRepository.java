package com.landgo.userservice.repository;

import com.landgo.userservice.entity.NewsletterCampaignRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NewsletterCampaignRecipientRepository extends JpaRepository<NewsletterCampaignRecipient, UUID> {

    /**
     * Subscribers already emailed for this campaign.
     *
     * <p>A retried campaign skips these, so recovering an interrupted broadcast cannot deliver
     * the same newsletter twice.
     */
    @Query("""
            SELECT r.subscriberId FROM NewsletterCampaignRecipient r
            WHERE r.campaignId = :campaignId AND r.status = 'SENT'
            """)
    List<UUID> findDeliveredSubscriberIds(@Param("campaignId") UUID campaignId);

    long countByCampaignIdAndStatus(UUID campaignId, String status);
}

package com.landgo.userservice.repository;

import com.landgo.userservice.entity.NewsletterCampaignRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NewsletterCampaignRecipientRepository extends JpaRepository<NewsletterCampaignRecipient, UUID> {
}

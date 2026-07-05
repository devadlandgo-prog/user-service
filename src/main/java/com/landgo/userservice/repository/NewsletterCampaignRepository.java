package com.landgo.userservice.repository;

import com.landgo.userservice.entity.NewsletterCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NewsletterCampaignRepository extends JpaRepository<NewsletterCampaign, UUID> {
}

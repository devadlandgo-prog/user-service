package com.landgo.userservice.repository;

import com.landgo.userservice.entity.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, UUID> {
    Optional<NewsletterSubscriber> findByEmail(String email);

    List<NewsletterSubscriber> findByStatus(String status);

    Optional<NewsletterSubscriber> findByUnsubscribeToken(UUID unsubscribeToken);

    /** Subscribers eligible to receive a newsletter: active and having given consent. */
    List<NewsletterSubscriber> findByStatusAndConsentTrue(String status);

    long countByStatusAndConsentTrue(String status);
}

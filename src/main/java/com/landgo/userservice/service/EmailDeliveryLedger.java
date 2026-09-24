package com.landgo.userservice.service;

import com.landgo.userservice.entity.EmailDelivery;
import com.landgo.userservice.repository.EmailDeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Claim-and-record bookkeeping for transactional email.
 *
 * <p>Each method runs in its own transaction so the record of an attempt survives whatever happens
 * to the caller's transaction — including a rollback. That is the point: if a payment transaction
 * rolls back after the receipt went out, the delivery record must still show that it went out.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailDeliveryLedger {

    private final EmailDeliveryRepository repository;

    /**
     * Reserves a key for sending.
     *
     * @return the claimed record, or empty when this event has already been delivered
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<EmailDelivery> claim(String idempotencyKey, String toEmail, String subject, String templateName) {
        Optional<EmailDelivery> existing = repository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            EmailDelivery delivery = existing.get();
            if (delivery.getStatus() == EmailDelivery.Status.SENT) {
                log.info("Email for key={} already delivered — skipping duplicate send", idempotencyKey);
                return Optional.empty();
            }
            // PENDING or FAILED: a previous attempt did not complete, so retry it.
            delivery.setAttempts(delivery.getAttempts() + 1);
            delivery.setStatus(EmailDelivery.Status.PENDING);
            return Optional.of(repository.save(delivery));
        }

        try {
            return Optional.of(repository.save(EmailDelivery.builder()
                    .idempotencyKey(idempotencyKey)
                    .toEmail(toEmail)
                    .subject(subject)
                    .templateName(templateName)
                    .status(EmailDelivery.Status.PENDING)
                    .attempts(1)
                    .build()));
        } catch (DataIntegrityViolationException e) {
            // Another thread or instance claimed the same key between the read and the insert.
            // The unique constraint is what makes concurrent delivery safe; losing the race means
            // the other side owns this send.
            log.info("Email key={} claimed concurrently — leaving the send to the other caller", idempotencyKey);
            return Optional.empty();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSent(EmailDelivery delivery) {
        delivery.setStatus(EmailDelivery.Status.SENT);
        delivery.setSentAt(LocalDateTime.now());
        delivery.setLastError(null);
        repository.save(delivery);
    }

    /**
     * Records a failure.
     *
     * <p>Only the provider's error text is stored — never the rendered body, a verification code
     * or a reset token.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(EmailDelivery delivery, String error) {
        delivery.setStatus(EmailDelivery.Status.FAILED);
        delivery.setLastError(error != null && error.length() > 2000 ? error.substring(0, 2000) : error);
        repository.save(delivery);
    }
}

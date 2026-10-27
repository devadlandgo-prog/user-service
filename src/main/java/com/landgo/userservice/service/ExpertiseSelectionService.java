package com.landgo.userservice.service;

import com.landgo.userservice.entity.Expertise;
import com.landgo.userservice.exception.ApiException;
import com.landgo.userservice.exception.BadRequestException;
import com.landgo.userservice.repository.ExpertiseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Canonicalises and authorises the expertise categories a market professional selects.
 *
 * <p>The cap used to live only in the mobile client, which derived it from the plan's name. Nothing
 * on the server checked it, so a direct API call could claim any number of categories, and an admin
 * changing a plan could not change what a professional was allowed to pick. The allowance now comes
 * from payment-service.
 *
 * <p>A live professional subscription is required: expertise is a paid entitlement, so no plan
 * means no allowance to spend. Nothing here trims or deletes an existing selection - a subscriber
 * who downgrades keeps what they saved and is asked to fit the new allowance the next time they
 * submit, which is what {@code EXPERTISE_LIMIT_EXCEEDED} tells them to do.
 *
 * <p>Three outcomes a caller has to tell apart: {@code 403
 * PROFESSIONAL_SUBSCRIPTION_REQUIRED} is a confirmed no - payment-service answered and the user has
 * no live plan. {@code 503 EXPERTISE_VERIFICATION_UNAVAILABLE} is "ask again later" - it did not
 * answer at all. {@code 400 EXPERTISE_LIMIT_EXCEEDED} is a plan they do hold, exceeded.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExpertiseSelectionService {

    private final ExpertiseRepository expertiseRepository;
    private final PaymentServiceClient paymentServiceClient;

    /**
     * Resolves a submitted selection into the exact values to persist.
     *
     * <p>Order matters: an unknown category is rejected before the count is judged, so a caller
     * sending one typo and nine valid categories is told about the typo rather than about a limit.
     *
     * @return canonical catalogue names, de-duplicated, in the order first submitted
     */
    @Transactional(readOnly = true)
    public List<String> resolveForPersist(UUID userId, List<String> submitted) {
        List<String> canonical = canonicalise(submitted);

        if (canonical.isEmpty()) {
            throw new BadRequestException("Select at least one area of expertise", "EXPERTISE_REQUIRED");
        }

        PaymentServiceClient.ExpertiseEntitlement entitlement = paymentServiceClient.getExpertiseEntitlement(userId);

        if (!entitlement.known()) {
            // payment-service did not answer, so the entitlement is unknown rather than absent.
            //
            // This used to accept the selection and log it as unverified, which made an outage the
            // one window in which someone with no subscription at all could write expertise - the
            // 403 below cannot run either when the entitlement cannot be fetched. Refusing is a
            // retry, not a rejection, and the distinct status says so: nothing is persisted and the
            // caller should offer "try again", not "buy a plan".
            log.warn("Refusing expertise write for {} ({} selected): payment-service unavailable",
                    userId, canonical.size());
            throw new ApiException(
                    "We can't verify your subscription right now. Please try again in a moment.",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "EXPERTISE_VERIFICATION_UNAVAILABLE");
        }

        if (!entitlement.active()) {
            // Expertise is a paid entitlement, so without a live plan there is no allowance to
            // spend and the write is refused rather than recorded. This is the server half of the
            // gate mobile already puts in front of the form: a profile cannot be created or its
            // expertise edited until a professional plan is active.
            throw new ApiException(
                    "An active professional subscription is required to select areas of expertise",
                    HttpStatus.FORBIDDEN,
                    "PROFESSIONAL_SUBSCRIPTION_REQUIRED");
        }

        if (entitlement.unlimited()) {
            return canonical;
        }

        int allowed = entitlement.limit() == null ? 0 : entitlement.limit();
        if (canonical.size() > allowed) {
            Map<String, Object> details = new HashMap<>();
            details.put("maxExpertiseSelections", allowed);
            details.put("selectedCount", canonical.size());
            throw new ApiException(
                    "Your plan allows " + allowed + " area" + (allowed == 1 ? "" : "s")
                            + " of expertise; " + canonical.size() + " were selected",
                    HttpStatus.BAD_REQUEST,
                    "EXPERTISE_LIMIT_EXCEEDED",
                    details);
        }

        return canonical;
    }

    /**
     * Maps each submitted value onto its catalogue name and drops repeats.
     *
     * <p>Matching ignores case, spacing and separators because the same category arrives as
     * "Land Surveyor", "land-surveyor" and "LAND_SURVEYOR" from different screens and drafts.
     */
    private List<String> canonicalise(List<String> submitted) {
        if (submitted == null) {
            return List.of();
        }

        Map<String, String> catalogueByKey = new HashMap<>();
        for (Expertise option : expertiseRepository.findByActiveTrue()) {
            catalogueByKey.put(matchKey(option.getName()), option.getName());
        }

        Map<String, String> accepted = new LinkedHashMap<>();
        List<String> unknown = new ArrayList<>();
        for (String raw : submitted) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String key = matchKey(raw);
            String canonical = catalogueByKey.get(key);
            if (canonical == null) {
                unknown.add(raw.trim());
                continue;
            }
            accepted.putIfAbsent(key, canonical);
        }

        if (!unknown.isEmpty()) {
            Map<String, Object> details = new HashMap<>();
            details.put("unknownExpertise", unknown);
            throw new ApiException(
                    "Unknown area of expertise: " + String.join(", ", unknown),
                    HttpStatus.BAD_REQUEST,
                    "EXPERTISE_INVALID",
                    details);
        }

        return new ArrayList<>(accepted.values());
    }

    private String matchKey(String value) {
        return value.trim().toUpperCase().replaceAll("[^A-Z0-9]+", "_").replaceAll("^_|_$", "");
    }
}

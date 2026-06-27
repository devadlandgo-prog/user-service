package com.landgo.userservice.repository;

import com.landgo.userservice.entity.User;
import com.landgo.userservice.entity.VendorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorProfileRepository extends JpaRepository<VendorProfile, UUID> {
    Optional<VendorProfile> findByUser(User user);
    java.util.List<VendorProfile> findAllByIdIn(java.util.Collection<UUID> ids);

    org.springframework.data.domain.Page<VendorProfile> findByVerifiedTrue(org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT vp.* FROM users.vendor_profiles vp WHERE vp.verified = true " +
           "AND EXISTS (SELECT 1 FROM payments.subscriptions s WHERE s.user_id = vp.user_id AND s.status = 'ACTIVE' AND LOWER(s.plan_category) = 'market_profession' AND s.end_date > CURRENT_TIMESTAMP) " +
           "AND (:specialization IS NULL OR :specialization = '' OR EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(REPLACE(REPLACE(s, ' ', '_'), '-', '_')) = LOWER(REPLACE(REPLACE(:specialization, ' ', '_'), '-', '_')))) " +
           "ORDER BY " +
           "  CASE WHEN :sortBy = 'rating' AND :sortDir = 'asc' THEN vp.rating END ASC NULLS LAST, " +
           "  CASE WHEN :sortBy = 'rating' AND :sortDir = 'desc' THEN vp.rating END DESC NULLS LAST, " +
           "  CASE WHEN :sortBy = 'totalReviews' AND :sortDir = 'asc' THEN vp.total_reviews END ASC NULLS LAST, " +
           "  CASE WHEN :sortBy = 'totalReviews' AND :sortDir = 'desc' THEN vp.total_reviews END DESC NULLS LAST, " +
           "  CASE WHEN :sortBy = 'yearsOfExperience' AND :sortDir = 'asc' THEN vp.years_of_experience END ASC NULLS LAST, " +
           "  CASE WHEN :sortBy = 'yearsOfExperience' AND :sortDir = 'desc' THEN vp.years_of_experience END DESC NULLS LAST, " +
           "  CASE WHEN :sortBy = 'createdAt' AND :sortDir = 'asc' THEN vp.created_at END ASC, " +
           "  CASE WHEN :sortBy = 'createdAt' AND :sortDir = 'desc' THEN vp.created_at END DESC",
           countQuery = "SELECT count(*) FROM users.vendor_profiles vp WHERE vp.verified = true " +
           "AND EXISTS (SELECT 1 FROM payments.subscriptions s WHERE s.user_id = vp.user_id AND s.status = 'ACTIVE' AND LOWER(s.plan_category) = 'market_profession' AND s.end_date > CURRENT_TIMESTAMP) " +
           "AND (:specialization IS NULL OR :specialization = '' OR EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(REPLACE(REPLACE(s, ' ', '_'), '-', '_')) = LOWER(REPLACE(REPLACE(:specialization, ' ', '_'), '-', '_'))))",
           nativeQuery = true)
    org.springframework.data.domain.Page<VendorProfile> findVerifiedProfessionals(
            @org.springframework.data.repository.query.Param("specialization") String specialization,
            @org.springframework.data.repository.query.Param("sortBy") String sortBy,
            @org.springframework.data.repository.query.Param("sortDir") String sortDir,
            org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT vp.* FROM users.vendor_profiles vp JOIN users.users u ON vp.user_id = u.id WHERE vp.verified = true " +
           "AND EXISTS (SELECT 1 FROM payments.subscriptions s WHERE s.user_id = vp.user_id AND s.status = 'ACTIVE' AND LOWER(s.plan_category) = 'market_profession' AND s.end_date > CURRENT_TIMESTAMP) " +
           "AND (:q IS NULL OR :q = '' OR (" +
           "LOWER(u.full_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.company_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.bio) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.business_city) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(REPLACE(REPLACE(s, ' ', '_'), '-', '_')) LIKE LOWER(CONCAT('%', REPLACE(REPLACE(:q, ' ', '_'), '-', '_'), '%'))))) " +
           "AND (:specialization IS NULL OR :specialization = '' OR EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(REPLACE(REPLACE(s, ' ', '_'), '-', '_')) = LOWER(REPLACE(REPLACE(:specialization, ' ', '_'), '-', '_')))) " +
           "ORDER BY " +
           "  CASE WHEN :sortBy = 'rating' AND :sortDir = 'asc' THEN vp.rating END ASC NULLS LAST, " +
           "  CASE WHEN :sortBy = 'rating' AND :sortDir = 'desc' THEN vp.rating END DESC NULLS LAST, " +
           "  CASE WHEN :sortBy = 'totalReviews' AND :sortDir = 'asc' THEN vp.total_reviews END ASC NULLS LAST, " +
           "  CASE WHEN :sortBy = 'totalReviews' AND :sortDir = 'desc' THEN vp.total_reviews END DESC NULLS LAST, " +
           "  CASE WHEN :sortBy = 'yearsOfExperience' AND :sortDir = 'asc' THEN vp.years_of_experience END ASC NULLS LAST, " +
           "  CASE WHEN :sortBy = 'yearsOfExperience' AND :sortDir = 'desc' THEN vp.years_of_experience END DESC NULLS LAST, " +
           "  CASE WHEN :sortBy = 'createdAt' AND :sortDir = 'asc' THEN vp.created_at END ASC, " +
           "  CASE WHEN :sortBy = 'createdAt' AND :sortDir = 'desc' THEN vp.created_at END DESC",
           countQuery = "SELECT count(*) FROM users.vendor_profiles vp JOIN users.users u ON vp.user_id = u.id WHERE vp.verified = true " +
           "AND EXISTS (SELECT 1 FROM payments.subscriptions s WHERE s.user_id = vp.user_id AND s.status = 'ACTIVE' AND LOWER(s.plan_category) = 'market_profession' AND s.end_date > CURRENT_TIMESTAMP) " +
           "AND (:q IS NULL OR :q = '' OR (" +
           "LOWER(u.full_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.company_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.bio) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.business_city) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(REPLACE(REPLACE(s, ' ', '_'), '-', '_')) LIKE LOWER(CONCAT('%', REPLACE(REPLACE(:q, ' ', '_'), '-', '_'), '%'))))) " +
           "AND (:specialization IS NULL OR :specialization = '' OR EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(REPLACE(REPLACE(s, ' ', '_'), '-', '_')) = LOWER(REPLACE(REPLACE(:specialization, ' ', '_'), '-', '_'))))",
           nativeQuery = true)
    org.springframework.data.domain.Page<VendorProfile> searchProfessionalsCombined(
            @org.springframework.data.repository.query.Param("q") String q,
            @org.springframework.data.repository.query.Param("specialization") String specialization,
            @org.springframework.data.repository.query.Param("sortBy") String sortBy,
            @org.springframework.data.repository.query.Param("sortDir") String sortDir,
            org.springframework.data.domain.Pageable pageable);

    @Modifying
    @Query("UPDATE VendorProfile vp SET vp.viewCount = vp.viewCount + 1 WHERE vp.id = :id")
    void incrementViewCount(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE VendorProfile vp SET vp.totalCalls = vp.totalCalls + 1 WHERE vp.id = :id")
    void incrementCallCount(@Param("id") UUID id);
}

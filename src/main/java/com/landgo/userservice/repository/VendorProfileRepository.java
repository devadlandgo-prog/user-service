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

    @org.springframework.data.jpa.repository.Query(value = "SELECT vp.* FROM users.vendor_profiles vp WHERE vp.verified = true ORDER BY vp.created_at DESC",
           countQuery = "SELECT count(*) FROM users.vendor_profiles vp WHERE vp.verified = true",
           nativeQuery = true)
    org.springframework.data.domain.Page<VendorProfile> findByVerifiedTrueOrderByCreatedAtDesc(org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT vp.* FROM users.vendor_profiles vp WHERE vp.verified = true ORDER BY vp.rating DESC NULLS LAST, vp.created_at DESC",
           countQuery = "SELECT count(*) FROM users.vendor_profiles vp WHERE vp.verified = true",
           nativeQuery = true)
    org.springframework.data.domain.Page<VendorProfile> findByVerifiedTrueOrderByRatingDesc(org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT vp.* FROM users.vendor_profiles vp WHERE vp.verified = true ORDER BY vp.total_reviews DESC NULLS LAST, vp.created_at DESC",
           countQuery = "SELECT count(*) FROM users.vendor_profiles vp WHERE vp.verified = true",
           nativeQuery = true)
    org.springframework.data.domain.Page<VendorProfile> findByVerifiedTrueOrderByTotalReviewsDesc(org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT vp.* FROM users.vendor_profiles vp WHERE vp.verified = true ORDER BY vp.years_of_experience DESC NULLS LAST, vp.created_at DESC",
           countQuery = "SELECT count(*) FROM users.vendor_profiles vp WHERE vp.verified = true",
           nativeQuery = true)
    org.springframework.data.domain.Page<VendorProfile> findByVerifiedTrueOrderByYearsOfExperienceDesc(org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT vp.* FROM users.vendor_profiles vp WHERE vp.verified = true " +
           "AND (:specialization IS NULL OR EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(s) = LOWER(:specialization)))",
           countQuery = "SELECT count(*) FROM users.vendor_profiles vp WHERE vp.verified = true " +
           "AND (:specialization IS NULL OR EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(s) = LOWER(:specialization)))",
           nativeQuery = true)
    org.springframework.data.domain.Page<VendorProfile> findByVerifiedTrueAndSpecialization(
            @org.springframework.data.repository.query.Param("specialization") String specialization,
            org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT vp.* FROM users.vendor_profiles vp WHERE vp.verified = true " +
           "AND EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(s) = LOWER(:specialization)) " +
           "ORDER BY vp.created_at DESC",
           countQuery = "SELECT count(*) FROM users.vendor_profiles vp WHERE vp.verified = true " +
           "AND EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(s) = LOWER(:specialization))",
           nativeQuery = true)
    org.springframework.data.domain.Page<VendorProfile> findByVerifiedTrueAndSpecializationOrderByCreatedAtDesc(
            @org.springframework.data.repository.query.Param("specialization") String specialization,
            org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT vp.* FROM users.vendor_profiles vp WHERE vp.verified = true " +
           "AND EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(s) = LOWER(:specialization)) " +
           "ORDER BY vp.rating DESC NULLS LAST, vp.created_at DESC",
           countQuery = "SELECT count(*) FROM users.vendor_profiles vp WHERE vp.verified = true " +
           "AND EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(s) = LOWER(:specialization))",
           nativeQuery = true)
    org.springframework.data.domain.Page<VendorProfile> findByVerifiedTrueAndSpecializationOrderByRatingDesc(
            @org.springframework.data.repository.query.Param("specialization") String specialization,
            org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT vp.* FROM users.vendor_profiles vp WHERE vp.verified = true " +
           "AND EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(s) = LOWER(:specialization)) " +
           "ORDER BY vp.total_reviews DESC NULLS LAST, vp.created_at DESC",
           countQuery = "SELECT count(*) FROM users.vendor_profiles vp WHERE vp.verified = true " +
           "AND EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(s) = LOWER(:specialization))",
           nativeQuery = true)
    org.springframework.data.domain.Page<VendorProfile> findByVerifiedTrueAndSpecializationOrderByTotalReviewsDesc(
            @org.springframework.data.repository.query.Param("specialization") String specialization,
            org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT vp.* FROM users.vendor_profiles vp WHERE vp.verified = true " +
           "AND EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(s) = LOWER(:specialization)) " +
           "ORDER BY vp.years_of_experience DESC NULLS LAST, vp.created_at DESC",
           countQuery = "SELECT count(*) FROM users.vendor_profiles vp WHERE vp.verified = true " +
           "AND EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(s) = LOWER(:specialization))",
           nativeQuery = true)
    org.springframework.data.domain.Page<VendorProfile> findByVerifiedTrueAndSpecializationOrderByYearsOfExperienceDesc(
            @org.springframework.data.repository.query.Param("specialization") String specialization,
            org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT vp.* FROM users.vendor_profiles vp JOIN users.users u ON vp.user_id = u.id WHERE vp.verified = true AND (" +
           "LOWER(u.full_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.company_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.bio) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.business_city) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(s) LIKE LOWER(CONCAT('%', :q, '%'))))",
           countQuery = "SELECT count(*) FROM users.vendor_profiles vp JOIN users.users u ON vp.user_id = u.id WHERE vp.verified = true AND (" +
           "LOWER(u.full_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.company_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.bio) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.business_city) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(s) LIKE LOWER(CONCAT('%', :q, '%'))))",
           nativeQuery = true)
    org.springframework.data.domain.Page<VendorProfile> searchProfessionals(@org.springframework.data.repository.query.Param("q") String query, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT vp.* FROM users.vendor_profiles vp JOIN users.users u ON vp.user_id = u.id WHERE vp.verified = true AND (" +
           "LOWER(u.full_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.company_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.bio) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.business_city) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(s) LIKE LOWER(CONCAT('%', :q, '%')))) " +
           "ORDER BY vp.created_at DESC",
           countQuery = "SELECT count(*) FROM users.vendor_profiles vp JOIN users.users u ON vp.user_id = u.id WHERE vp.verified = true AND (" +
           "LOWER(u.full_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.company_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.bio) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.business_city) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(s) LIKE LOWER(CONCAT('%', :q, '%'))))",
           nativeQuery = true)
    org.springframework.data.domain.Page<VendorProfile> searchProfessionalsOrderByCreatedAtDesc(
            @org.springframework.data.repository.query.Param("q") String query,
            org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT vp.* FROM users.vendor_profiles vp JOIN users.users u ON vp.user_id = u.id WHERE vp.verified = true AND (" +
           "LOWER(u.full_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.company_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.bio) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.business_city) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(s) LIKE LOWER(CONCAT('%', :q, '%')))) " +
           "ORDER BY vp.rating DESC NULLS LAST, vp.created_at DESC",
           countQuery = "SELECT count(*) FROM users.vendor_profiles vp JOIN users.users u ON vp.user_id = u.id WHERE vp.verified = true AND (" +
           "LOWER(u.full_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.company_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.bio) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.business_city) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(s) LIKE LOWER(CONCAT('%', :q, '%'))))",
           nativeQuery = true)
    org.springframework.data.domain.Page<VendorProfile> searchProfessionalsOrderByRatingDesc(
            @org.springframework.data.repository.query.Param("q") String query,
            org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT vp.* FROM users.vendor_profiles vp JOIN users.users u ON vp.user_id = u.id WHERE vp.verified = true AND (" +
           "LOWER(u.full_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.company_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.bio) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.business_city) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(s) LIKE LOWER(CONCAT('%', :q, '%')))) " +
           "ORDER BY vp.total_reviews DESC NULLS LAST, vp.created_at DESC",
           countQuery = "SELECT count(*) FROM users.vendor_profiles vp JOIN users.users u ON vp.user_id = u.id WHERE vp.verified = true AND (" +
           "LOWER(u.full_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.company_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.bio) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.business_city) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(s) LIKE LOWER(CONCAT('%', :q, '%'))))",
           nativeQuery = true)
    org.springframework.data.domain.Page<VendorProfile> searchProfessionalsOrderByTotalReviewsDesc(
            @org.springframework.data.repository.query.Param("q") String query,
            org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT vp.* FROM users.vendor_profiles vp JOIN users.users u ON vp.user_id = u.id WHERE vp.verified = true AND (" +
           "LOWER(u.full_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.company_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.bio) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.business_city) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(s) LIKE LOWER(CONCAT('%', :q, '%')))) " +
           "ORDER BY vp.years_of_experience DESC NULLS LAST, vp.created_at DESC",
           countQuery = "SELECT count(*) FROM users.vendor_profiles vp JOIN users.users u ON vp.user_id = u.id WHERE vp.verified = true AND (" +
           "LOWER(u.full_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.company_name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.bio) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.business_city) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "EXISTS (SELECT 1 FROM unnest(vp.specialization) s WHERE LOWER(s) LIKE LOWER(CONCAT('%', :q, '%'))))",
           nativeQuery = true)
    org.springframework.data.domain.Page<VendorProfile> searchProfessionalsOrderByYearsOfExperienceDesc(
            @org.springframework.data.repository.query.Param("q") String query,
            org.springframework.data.domain.Pageable pageable);

    @Modifying
    @Query("UPDATE VendorProfile vp SET vp.viewCount = vp.viewCount + 1 WHERE vp.id = :id")
    void incrementViewCount(@Param("id") UUID id);
}

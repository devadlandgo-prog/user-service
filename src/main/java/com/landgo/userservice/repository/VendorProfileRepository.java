package com.landgo.userservice.repository;

import com.landgo.userservice.entity.User;
import com.landgo.userservice.entity.VendorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorProfileRepository extends JpaRepository<VendorProfile, UUID> {
    Optional<VendorProfile> findByUser(User user);
    java.util.List<VendorProfile> findAllByIdIn(java.util.Collection<UUID> ids);
    
    org.springframework.data.domain.Page<VendorProfile> findByVerifiedTrue(org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT vp FROM VendorProfile vp JOIN vp.user u WHERE vp.verified = true AND (" +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.companyName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.bio) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(vp.businessCity) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "EXISTS (SELECT s FROM vp.specialization s WHERE LOWER(s) LIKE LOWER(CONCAT('%', :q, '%'))))")
    org.springframework.data.domain.Page<VendorProfile> searchProfessionals(@org.springframework.data.repository.query.Param("q") String query, org.springframework.data.domain.Pageable pageable);
}

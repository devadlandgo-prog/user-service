package com.landgo.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "vendor_profiles")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class VendorProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Column(name = "company_description", columnDefinition = "TEXT")
    private String companyDescription;

    @Column(name = "company_logo", length = 500)
    private String companyLogo;

    @Column(name = "business_license")
    private String businessLicense;

    @Column(name = "business_address", nullable = false)
    private String businessAddress;

    @Column(name = "business_city", nullable = false, length = 100)
    private String businessCity;

    @Column(name = "business_state", nullable = false, length = 100)
    private String businessState;

    @Column(name = "business_zip_code", nullable = false, length = 20)
    private String businessZipCode;

    @Column(name = "business_country", nullable = false, length = 100)
    private String businessCountry;

    @Column(name = "website")
    private String website;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "specialization", columnDefinition = "text[]")
    private List<String> specialization;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "service_area", columnDefinition = "text[]")
    private List<String> serviceArea;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "certifications", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<VendorCertification> certifications;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "verified")
    @Builder.Default
    private boolean verified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", length = 20)
    @Builder.Default
    private com.landgo.userservice.enums.VerificationStatus verificationStatus = com.landgo.userservice.enums.VerificationStatus.PENDING;

    @Column(name = "verification_notes", length = 500)
    private String verificationNotes;

    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "total_listings")
    @Builder.Default
    private Integer totalListings = 0;

    @Column(name = "total_sold")
    @Builder.Default
    private Integer totalSold = 0;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;
}
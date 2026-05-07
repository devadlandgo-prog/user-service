package com.landgo.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "vendor_profiles")
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class VendorProfile extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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

    @Column(name = "verified")
    @Builder.Default
    private boolean verified = false;

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
}
package com.landgo.userservice.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class VendorResponse {
    private UUID id;
    private UUID userId;
    private String companyName;
    private String companyDescription;
    private String companyLogo;
    private String businessAddress;
    private String businessCity;
    private String businessState;
    private String businessZipCode;
    private String businessCountry;
    private String website;
    private boolean verified;
    private BigDecimal rating;
    private Integer totalReviews;
    private Integer totalListings;
    private Integer totalSold;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.landgo.userservice.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JSON shape returned by core-service {@code GET /internal/vendors/{id}/listing-stats}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoreVendorListingStatsDto {
    private long activeListings;
    private long totalListings;
    private long totalViews;
    private long totalEnquiries;
}

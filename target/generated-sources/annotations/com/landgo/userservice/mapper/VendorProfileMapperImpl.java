package com.landgo.userservice.mapper;

import com.landgo.userservice.dto.request.VendorProfileRequest;
import com.landgo.userservice.dto.response.VendorResponse;
import com.landgo.userservice.entity.VendorProfile;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-28T19:29:21+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class VendorProfileMapperImpl implements VendorProfileMapper {

    @Override
    public VendorResponse toResponse(VendorProfile vendor) {
        if ( vendor == null ) {
            return null;
        }

        VendorResponse.VendorResponseBuilder vendorResponse = VendorResponse.builder();

        vendorResponse.businessAddress( vendor.getBusinessAddress() );
        vendorResponse.businessCity( vendor.getBusinessCity() );
        vendorResponse.businessCountry( vendor.getBusinessCountry() );
        vendorResponse.businessState( vendor.getBusinessState() );
        vendorResponse.businessZipCode( vendor.getBusinessZipCode() );
        vendorResponse.companyDescription( vendor.getCompanyDescription() );
        vendorResponse.companyLogo( vendor.getCompanyLogo() );
        vendorResponse.companyName( vendor.getCompanyName() );
        vendorResponse.createdAt( vendor.getCreatedAt() );
        vendorResponse.id( vendor.getId() );
        vendorResponse.rating( vendor.getRating() );
        vendorResponse.totalListings( vendor.getTotalListings() );
        vendorResponse.totalReviews( vendor.getTotalReviews() );
        vendorResponse.totalSold( vendor.getTotalSold() );
        vendorResponse.updatedAt( vendor.getUpdatedAt() );
        vendorResponse.verified( vendor.isVerified() );
        vendorResponse.website( vendor.getWebsite() );

        vendorResponse.userId( vendor.getUser().getId() );

        return vendorResponse.build();
    }

    @Override
    public VendorProfile toEntity(VendorProfileRequest request) {
        if ( request == null ) {
            return null;
        }

        VendorProfile.VendorProfileBuilder<?, ?> vendorProfile = VendorProfile.builder();

        vendorProfile.businessAddress( request.getBusinessAddress() );
        vendorProfile.businessCity( request.getBusinessCity() );
        vendorProfile.businessCountry( request.getBusinessCountry() );
        vendorProfile.businessLicense( request.getBusinessLicense() );
        vendorProfile.businessState( request.getBusinessState() );
        vendorProfile.businessZipCode( request.getBusinessZipCode() );
        vendorProfile.companyDescription( request.getCompanyDescription() );
        vendorProfile.companyLogo( request.getCompanyLogo() );
        vendorProfile.companyName( request.getCompanyName() );
        vendorProfile.website( request.getWebsite() );

        return vendorProfile.build();
    }
}

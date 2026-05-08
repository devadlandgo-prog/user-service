package com.landgo.userservice.mapper;

import com.landgo.userservice.dto.request.VendorProfileRequest;
import com.landgo.userservice.dto.response.VendorResponse;
import com.landgo.userservice.entity.User;
import com.landgo.userservice.entity.VendorProfile;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-08T02:14:48+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Homebrew)"
)
@Component
public class VendorProfileMapperImpl implements VendorProfileMapper {

    @Override
    public VendorResponse toResponse(VendorProfile vendor) {
        if ( vendor == null ) {
            return null;
        }

        VendorResponse.VendorResponseBuilder vendorResponse = VendorResponse.builder();

        vendorResponse.userId( vendorUserId( vendor ) );
        vendorResponse.id( vendor.getId() );
        vendorResponse.companyName( vendor.getCompanyName() );
        vendorResponse.companyDescription( vendor.getCompanyDescription() );
        vendorResponse.companyLogo( vendor.getCompanyLogo() );
        vendorResponse.businessAddress( vendor.getBusinessAddress() );
        vendorResponse.businessCity( vendor.getBusinessCity() );
        vendorResponse.businessState( vendor.getBusinessState() );
        vendorResponse.businessZipCode( vendor.getBusinessZipCode() );
        vendorResponse.businessCountry( vendor.getBusinessCountry() );
        vendorResponse.website( vendor.getWebsite() );
        vendorResponse.verified( vendor.isVerified() );
        vendorResponse.rating( vendor.getRating() );
        vendorResponse.totalReviews( vendor.getTotalReviews() );
        vendorResponse.totalListings( vendor.getTotalListings() );
        vendorResponse.totalSold( vendor.getTotalSold() );
        vendorResponse.createdAt( vendor.getCreatedAt() );
        vendorResponse.updatedAt( vendor.getUpdatedAt() );

        return vendorResponse.build();
    }

    @Override
    public VendorProfile toEntity(VendorProfileRequest request) {
        if ( request == null ) {
            return null;
        }

        VendorProfile.VendorProfileBuilder<?, ?> vendorProfile = VendorProfile.builder();

        vendorProfile.companyName( request.getCompanyName() );
        vendorProfile.companyDescription( request.getCompanyDescription() );
        vendorProfile.companyLogo( request.getCompanyLogo() );
        vendorProfile.businessLicense( request.getBusinessLicense() );
        vendorProfile.businessAddress( request.getBusinessAddress() );
        vendorProfile.businessCity( request.getBusinessCity() );
        vendorProfile.businessState( request.getBusinessState() );
        vendorProfile.businessZipCode( request.getBusinessZipCode() );
        vendorProfile.businessCountry( request.getBusinessCountry() );
        vendorProfile.website( request.getWebsite() );

        return vendorProfile.build();
    }

    private UUID vendorUserId(VendorProfile vendorProfile) {
        if ( vendorProfile == null ) {
            return null;
        }
        User user = vendorProfile.getUser();
        if ( user == null ) {
            return null;
        }
        UUID id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}

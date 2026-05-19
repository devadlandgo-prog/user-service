package com.landgo.userservice.mapper;

import com.landgo.userservice.dto.request.ProfessionalRegisterRequest;
import com.landgo.userservice.dto.request.CertificationRequest;
import com.landgo.userservice.dto.request.VendorProfileRequest;
import com.landgo.userservice.dto.response.CertificationResponse;
import com.landgo.userservice.dto.response.VendorResponse;
import com.landgo.userservice.entity.VendorCertification;
import com.landgo.userservice.entity.VendorProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VendorProfileMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "ownerName", source = "user.fullName")
    @Mapping(target = "ownerEmail", source = "user.email")
    VendorResponse toResponse(VendorProfile vendor);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "specialization", ignore = true)
    @Mapping(target = "yearsOfExperience", ignore = true)
    @Mapping(target = "serviceArea", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "verificationStatus", ignore = true)
    @Mapping(target = "verificationNotes", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "totalReviews", ignore = true)
    @Mapping(target = "totalListings", ignore = true)
    @Mapping(target = "totalSold", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    VendorProfile toEntity(VendorProfileRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "phoneNumber", source = "phone")
    @Mapping(target = "companyLogo", source = "profileImageUrl")
    @Mapping(target = "companyDescription", source = "companyDescription")
    @Mapping(target = "website", source = "website")
    @Mapping(target = "businessLicense", source = "licenseNumber")
    @Mapping(target = "businessAddress", constant = "TBD") // These are missing in unified request, can be updated later
    @Mapping(target = "businessCity", constant = "TBD")
    @Mapping(target = "businessState", constant = "TBD")
    @Mapping(target = "businessZipCode", constant = "TBD")
    @Mapping(target = "businessCountry", constant = "TBD")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "specialization", ignore = true)
    @Mapping(target = "yearsOfExperience", ignore = true)
    @Mapping(target = "serviceArea", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "verificationStatus", ignore = true)
    @Mapping(target = "verificationNotes", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "totalReviews", ignore = true)
    @Mapping(target = "totalListings", ignore = true)
    @Mapping(target = "totalSold", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    VendorProfile toEntity(ProfessionalRegisterRequest request);

    VendorCertification toEntity(CertificationRequest request);

    CertificationResponse toResponse(VendorCertification certification);
}

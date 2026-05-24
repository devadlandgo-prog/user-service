package com.landgo.userservice.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import jakarta.validation.Valid;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UpdateProfileRequest {
    @Size(min = 2, max = 100) private String fullName;
    @Size(max = 20) private String phone;
    @Size(max = 500) private String profileImageUrl;
    @Size(max = 200) private String location;
    @Size(max = 2000) private String professionalBio;
    @Size(max = 50) private String timezone;

    // Professional/Vendor fields
    @Size(max = 150) private String companyName;
    @Size(max = 50) private String licenseNumber;
    private java.util.List<String> specialization;
    private Integer yearsOfExperience;
    private java.util.List<String> serviceArea;
    @Valid
    private java.util.List<CertificationRequest> certifications;
    @Size(max = 1000) private String bio; // mapped to VendorProfile.bio
    @Size(max = 2000) private String companyDescription;
    @Size(max = 2000) private String companyLogo;

    // Additional address fields
    private String businessAddress;
    private String businessCity;
    private String businessState;
    private String businessZipCode;
    private String businessCountry;
    private String website;
}

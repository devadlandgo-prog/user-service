package com.landgo.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class VendorProfileRequest {
    @NotBlank @Size(min = 2, max = 100)
    private String companyName;

    private String companyDescription;

    private String companyLogo;

    private String businessLicense;

    @NotBlank
    private String businessAddress;

    @NotBlank
    private String businessCity;

    @NotBlank
    private String businessState;

    @NotBlank
    private String businessZipCode;

    @NotBlank
    private String businessCountry;

    private String website;
}

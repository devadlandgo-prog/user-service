package com.landgo.userservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProfessionalRegisterRequest {
    // Account details
    @Size(max = 100)
    private String fullName;

    @Email
    private String email;

    @Size(min = 8)
    private String password;

    private String phone;

    // Professional profile details
    @NotBlank(message = "Company name is required")
    @Size(max = 150)
    private String companyName;

    @NotBlank(message = "License number is required")
    @Size(max = 50)
    private String licenseNumber;

    @NotEmpty(message = "At least one specialization is required")
    private List<String> specialization;

    @NotNull(message = "Years of experience is required")
    @Min(0)
    private Integer yearsOfExperience;

    @NotEmpty(message = "Service area is required")
    private List<String> serviceArea;

    @Size(max = 2000)
    private String profileImageUrl;

    @Size(max = 1000)
    private String bio;

    private List<String> certifications;

    @Size(max = 2000)
    private String companyDescription;

    @Size(max = 500)
    private String website;
}

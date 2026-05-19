package com.landgo.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificationRequest {

    @NotBlank(message = "Certification title is required")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "Certification fileKey is required")
    @Size(max = 1000)
    private String fileKey;
}

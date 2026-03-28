package com.landgo.userservice.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UpdateProfileRequest {
    @Size(min = 2, max = 100) private String fullName;
    @Size(max = 20) private String phone;
    @Size(max = 500) private String profileImageUrl;
    @Size(max = 200) private String location;
    @Size(max = 2000) private String professionalBio;
}

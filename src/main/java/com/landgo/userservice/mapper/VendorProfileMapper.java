package com.landgo.userservice.mapper;

import com.landgo.userservice.dto.request.VendorProfileRequest;
import com.landgo.userservice.dto.response.VendorResponse;
import com.landgo.userservice.entity.VendorProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VendorProfileMapper {

    @Mapping(target = "userId", source = "user.id")
    VendorResponse toResponse(VendorProfile vendor);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    VendorProfile toEntity(VendorProfileRequest request);
}

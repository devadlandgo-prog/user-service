package com.landgo.userservice.mapper;

import com.landgo.userservice.dto.request.RegisterRequest;
import com.landgo.userservice.dto.response.UserResponse;
import com.landgo.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "isVendor", expression = "java(user.isVendor())")
    @Mapping(target = "isProfessional", expression = "java(user.isProfessional())")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "agentAuthorizationAccepted", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "professionalBio", ignore = true)
    @Mapping(target = "recoLicenseNumber", source = "licenseNumber")
    @Mapping(target = "userType", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "emailVerifiedAt", ignore = true)
    @Mapping(target = "mfaEnabled", ignore = true)
    @Mapping(target = "mfaVerified", ignore = true)
    @Mapping(target = "timezone", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "isProfessional", ignore = true)
    User toEntity(RegisterRequest request);
}

package com.landgo.userservice.mapper;

import com.landgo.userservice.dto.request.RegisterRequest;
import com.landgo.userservice.dto.response.UserResponse;
import com.landgo.userservice.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-28T19:29:22+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.active( user.isActive() );
        userResponse.agencyName( user.getAgencyName() );
        userResponse.authProvider( user.getAuthProvider() );
        userResponse.createdAt( user.getCreatedAt() );
        userResponse.email( user.getEmail() );
        userResponse.emailVerified( user.isEmailVerified() );
        userResponse.firstName( user.getFirstName() );
        userResponse.id( user.getId() );
        userResponse.lastName( user.getLastName() );
        userResponse.location( user.getLocation() );
        userResponse.phone( user.getPhone() );
        userResponse.professionalBio( user.getProfessionalBio() );
        userResponse.profileImageUrl( user.getProfileImageUrl() );
        userResponse.recoLicenseNumber( user.getRecoLicenseNumber() );
        userResponse.role( user.getRole() );
        userResponse.updatedAt( user.getUpdatedAt() );
        userResponse.userType( user.getUserType() );

        userResponse.isVendor( user.isVendor() );
        userResponse.isAgent( user.isAgent() );
        userResponse.fullName( user.getFullName() );

        return userResponse.build();
    }

    @Override
    public User toEntity(RegisterRequest request) {
        if ( request == null ) {
            return null;
        }

        User.UserBuilder<?, ?> user = User.builder();

        user.agencyName( request.getAgencyName() );
        user.authProvider( request.getAuthProvider() );
        user.email( request.getEmail() );
        user.firstName( request.getFirstName() );
        user.fullName( request.getFullName() );
        user.lastName( request.getLastName() );
        user.password( request.getPassword() );
        user.phone( request.getPhone() );
        user.profileImageUrl( request.getProfileImageUrl() );
        user.providerId( request.getProviderId() );
        user.recoLicenseNumber( request.getRecoLicenseNumber() );
        user.userType( request.getUserType() );

        user.emailVerified( false );
        user.active( true );

        return user.build();
    }
}

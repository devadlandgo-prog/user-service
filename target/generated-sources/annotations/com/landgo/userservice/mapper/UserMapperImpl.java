package com.landgo.userservice.mapper;

import com.landgo.userservice.dto.request.RegisterRequest;
import com.landgo.userservice.dto.response.UserResponse;
import com.landgo.userservice.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-08T02:14:48+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Homebrew)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.id( user.getId() );
        userResponse.userType( user.getUserType() );
        userResponse.firstName( user.getFirstName() );
        userResponse.lastName( user.getLastName() );
        userResponse.email( user.getEmail() );
        userResponse.phone( user.getPhone() );
        userResponse.profileImageUrl( user.getProfileImageUrl() );
        userResponse.location( user.getLocation() );
        userResponse.professionalBio( user.getProfessionalBio() );
        userResponse.authProvider( user.getAuthProvider() );
        userResponse.role( user.getRole() );
        userResponse.emailVerified( user.isEmailVerified() );
        userResponse.emailVerifiedAt( user.getEmailVerifiedAt() );
        userResponse.active( user.isActive() );
        userResponse.agencyName( user.getAgencyName() );
        userResponse.recoLicenseNumber( user.getRecoLicenseNumber() );
        userResponse.mfaEnabled( user.isMfaEnabled() );
        userResponse.mfaVerified( user.isMfaVerified() );
        userResponse.timezone( user.getTimezone() );
        userResponse.createdAt( user.getCreatedAt() );
        userResponse.updatedAt( user.getUpdatedAt() );

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

        user.recoLicenseNumber( request.getLicenseNumber() );
        user.fullName( request.getFullName() );
        user.email( request.getEmail() );
        user.password( request.getPassword() );
        user.phone( request.getPhone() );
        user.profileImageUrl( request.getProfileImageUrl() );
        user.authProvider( request.getAuthProvider() );
        user.providerId( request.getProviderId() );
        user.agencyName( request.getAgencyName() );

        user.emailVerified( false );
        user.active( true );

        return user.build();
    }
}

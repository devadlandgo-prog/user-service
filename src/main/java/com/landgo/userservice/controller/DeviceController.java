package com.landgo.userservice.controller;

import com.landgo.userservice.dto.response.ApiResponse;
import com.landgo.userservice.dto.request.DeviceTokenRequest;
import com.landgo.userservice.dto.response.DeviceTokenResponse;
import com.landgo.userservice.security.CurrentUser;
import com.landgo.userservice.security.UserPrincipal;
import com.landgo.userservice.service.DeviceTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceTokenService deviceTokenService;

    @PutMapping("/me/token")
    public ResponseEntity<ApiResponse<DeviceTokenResponse>> registerToken(
            @CurrentUser UserPrincipal userDetails,
            @Valid @RequestBody DeviceTokenRequest request) {
        
        DeviceTokenResponse response = deviceTokenService.registerToken(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/me/token")
    public ResponseEntity<ApiResponse<Void>> unregisterToken(
            @CurrentUser UserPrincipal userDetails,
            @Valid @RequestBody DeviceTokenRequest request) {
        
        deviceTokenService.unregisterToken(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

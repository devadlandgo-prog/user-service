# User Service API Reference (POST + GET)

Base URL (local): `http://localhost:8081`  
Common success wrapper:
```json
{
  "success": true,
  "message": "optional message",
  "data": {}
}
```

Common error wrapper:
```json
{
  "success": false,
  "message": "error description",
  "code": "ERROR_CODE"
}
```

## 1) Register User

- **Method:** `POST`
- **URL:** `/auth/register`
- **Auth required:** No
- **Query params:** None
- **Headers:** `Content-Type: application/json`

### Sample Request
```json
{
  "userType": "SELLER",
  "fullName": "Test User",
  "email": "test.user@example.com",
  "password": "Password@123",
  "confirmPassword": "Password@123",
  "phone": "+919999999999",
  "agencyName": "LandGo Realty",
  "recoLicenseNumber": "RECO-12345",
  "agentAuthorizationAccepted": true,
  "authProvider": "EMAIL"
}
```

### Sample Response (201)
```json
{
  "success": true,
  "message": "Registration successful. Please verify your email.",
  "data": {
    "id": "be80f28d-65e1-471f-ab45-5674db10e775",
    "userType": "SELLER",
    "fullName": "Test User",
    "firstName": "Test",
    "lastName": "User",
    "email": "test.user@example.com",
    "phone": "+919999999999",
    "profileImageUrl": null,
    "location": null,
    "professionalBio": null,
    "authProvider": "EMAIL",
    "role": "SELLER",
    "emailVerified": false,
    "active": true,
    "agencyName": "LandGo Realty",
    "recoLicenseNumber": "RECO-12345",
    "vendor": false,
    "agent": false,
    "createdAt": "2026-04-11T16:42:28.776809",
    "updatedAt": "2026-04-11T16:42:28.776809"
  }
}
```

## 2) Login

- **Method:** `POST`
- **URL:** `/auth/login`
- **Auth required:** No
- **Query params:** None
- **Headers:** `Content-Type: application/json`

### Sample Request
```json
{
  "email": "test.user@example.com",
  "password": "Password@123"
}
```

### Sample Response (200)
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzM4NCJ9....",
    "refreshToken": "eyJhbGciOiJIUzM4NCJ9....",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": "be80f28d-65e1-471f-ab45-5674db10e775",
      "userType": "SELLER",
      "fullName": "Test User",
      "firstName": "Test",
      "lastName": "User",
      "email": "test.user@example.com",
      "phone": null,
      "authProvider": "EMAIL",
      "role": "SELLER",
      "emailVerified": false,
      "active": true
    }
  }
}
```

## 3) OAuth2 Login

- **Method:** `POST`
- **URL:** `/auth/oauth2`
- **Auth required:** No
- **Query params:** None
- **Headers:** `Content-Type: application/json`

### Sample Request
```json
{
  "token": "oauth-id-token-or-access-token",
  "authProvider": "GOOGLE"
}
```

### Sample Response (200)
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzM4NCJ9....",
    "refreshToken": "eyJhbGciOiJIUzM4NCJ9....",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": "11111111-1111-1111-1111-111111111111",
      "email": "oauth.user@example.com",
      "authProvider": "GOOGLE",
      "role": "SELLER"
    }
  }
}
```

## 4) Logout

- **Method:** `POST`
- **URL:** `/auth/logout`
- **Auth required:** Yes (Bearer token)
- **Query params:** None
- **Headers:** `Authorization: Bearer <accessToken>`

### Sample Request Body
No body

### Sample Response (200)
```json
{
  "success": true,
  "message": "Logged out successfully",
  "data": null
}
```

## 5) Verify Email

- **Method:** `POST`
- **URL:** `/auth/verify`
- **Auth required:** No
- **Query params:** None
- **Headers:** `Content-Type: application/json`

### Sample Request
```json
{
  "email": "test.user@example.com",
  "code": "1234"
}
```

### Sample Response (200)
```json
{
  "success": true,
  "message": "Email verified successfully",
  "data": null
}
```

## 6) Resend Verification Code

- **Method:** `POST`
- **URL:** `/auth/resend-verification`
- **Auth required:** No
- **Query params:** None
- **Headers:** `Content-Type: application/json`

### Sample Request
```json
{
  "email": "test.user@example.com"
}
```

### Sample Response (200)
```json
{
  "success": true,
  "message": "Verification code sent",
  "data": null
}
```

## 7) Forgot Password

- **Method:** `POST`
- **URL:** `/auth/forgot-password`
- **Auth required:** No
- **Query params:** None
- **Headers:** `Content-Type: application/json`

### Sample Request
```json
{
  "email": "test.user@example.com"
}
```

### Sample Response (200)
```json
{
  "success": true,
  "message": "If an account exists, a reset link has been sent",
  "data": null
}
```

## 8) Validate Reset Token

- **Method:** `GET`
- **URL:** `/auth/reset-password/validate`
- **Auth required:** No
- **Query params:**
  - `token` (string, required)

### Sample Request URL
`/auth/reset-password/validate?token=abc-reset-token`

### Sample Response (200)
```json
{
  "success": true,
  "message": "Token is valid",
  "data": null
}
```

## 9) Reset Password

- **Method:** `POST`
- **URL:** `/auth/reset-password`
- **Auth required:** No
- **Query params:** None
- **Headers:** `Content-Type: application/json`

### Sample Request
```json
{
  "token": "abc-reset-token",
  "newPassword": "NewPassword@123"
}
```

### Sample Response (200)
```json
{
  "success": true,
  "message": "Password reset successfully",
  "data": null
}
```

## 10) Change Password

- **Method:** `POST`
- **URL:** `/auth/change-password`
- **Auth required:** Yes (Bearer token)
- **Query params:** None
- **Headers:** `Authorization: Bearer <accessToken>`, `Content-Type: application/json`

### Sample Request
```json
{
  "currentPassword": "Password@123",
  "newPassword": "NewPassword@123",
  "confirmPassword": "NewPassword@123"
}
```

### Sample Response (200)
```json
{
  "success": true,
  "message": "Password changed successfully",
  "data": null
}
```

## 11) Get My Profile

- **Method:** `GET`
- **URL:** `/profile`
- **Auth required:** Yes (Bearer token)
- **Query params:** None
- **Headers:** `Authorization: Bearer <accessToken>`

### Sample Response (200)
```json
{
  "success": true,
  "data": {
    "id": "be80f28d-65e1-471f-ab45-5674db10e775",
    "userType": "SELLER",
    "fullName": "Test User",
    "firstName": "Test",
    "lastName": "User",
    "email": "test.user@example.com",
    "phone": "+919999999999",
    "authProvider": "EMAIL",
    "role": "SELLER",
    "emailVerified": false,
    "active": true
  }
}
```

## 12) Get Notification Preferences

- **Method:** `GET`
- **URL:** `/users/me/notification-settings`
- **Auth required:** Yes (Bearer token)
- **Query params:** None
- **Headers:** `Authorization: Bearer <accessToken>`

### Sample Response (200)
```json
{
  "success": true,
  "data": {
    "id": "99999999-9999-9999-9999-999999999999",
    "listingAlerts": true,
    "priceDrops": true,
    "marketingEmails": false,
    "securityAlerts": true
  }
}
```

## 13) Get User By ID (Internal)

- **Method:** `GET`
- **URL:** `/internal/users/{userId}`
- **Auth required:** Internal endpoint (typically network/service secured)
- **Path params:**
  - `userId` (UUID, required)
- **Query params:** None

### Sample Request URL
`/internal/users/be80f28d-65e1-471f-ab45-5674db10e775`

### Sample Response (200)
```json
{
  "id": "be80f28d-65e1-471f-ab45-5674db10e775",
  "userType": "SELLER",
  "fullName": "Test User",
  "firstName": "Test",
  "lastName": "User",
  "email": "test.user@example.com",
  "phone": null,
  "profileImageUrl": null,
  "location": null,
  "professionalBio": null,
  "authProvider": "EMAIL",
  "role": "SELLER",
  "emailVerified": false,
  "active": true,
  "agencyName": null,
  "recoLicenseNumber": null,
  "vendor": false,
  "agent": false,
  "createdAt": "2026-04-11T16:42:28.776809",
  "updatedAt": "2026-04-11T16:42:28.776809"
}
```

## 14) Get Vendor Profile By User ID (Internal)

- **Method:** `GET`
- **URL:** `/internal/users/{userId}/vendor`
- **Auth required:** Internal endpoint (typically network/service secured)
- **Path params:**
  - `userId` (UUID, required)
- **Query params:** None

### Sample Request URL
`/internal/users/be80f28d-65e1-471f-ab45-5674db10e775/vendor`

### Sample Response (200)
```json
{
  "id": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "userId": "be80f28d-65e1-471f-ab45-5674db10e775",
  "companyName": "LandGo Realty",
  "companyDescription": "Premium land advisory",
  "companyLogo": "https://cdn.example.com/logo.png",
  "businessAddress": "123 Main Street",
  "businessCity": "Mumbai",
  "businessState": "Maharashtra",
  "businessZipCode": "400001",
  "businessCountry": "India",
  "website": "https://landgo.example.com",
  "verified": false,
  "rating": 4.5,
  "totalReviews": 10,
  "totalListings": 24,
  "totalSold": 4,
  "createdAt": "2026-04-01T10:00:00",
  "updatedAt": "2026-04-11T11:00:00"
}
```

## Quick Enum/Validation Reference

- `userType`: `SELLER` | `AGENT`
- `authProvider`: `EMAIL` | `GOOGLE` | `APPLE`
- internal role update endpoint (`PATCH /internal/users/{userId}/role`) accepts query `role`:
  `SELLER | VENDOR | AGENT | ADMIN` (not included above because this document is POST + GET only)

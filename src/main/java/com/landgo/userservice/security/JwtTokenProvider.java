package com.landgo.userservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j @Component
public class JwtTokenProvider {
    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.access-token-expiration:3600000}") long accessExp,
            @Value("${app.jwt.refresh-token-expiration:604800000}") long refreshExp) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessExp;
        this.refreshTokenExpiration = refreshExp;
    }
    public String generateAccessToken(UserPrincipal up) { return genToken(up.getId(), up.getRole().name(), accessTokenExpiration); }
    public String generateRefreshToken(UserPrincipal up) { return genToken(up.getId(), up.getRole().name(), refreshTokenExpiration); }
    private String genToken(UUID userId, String role, long exp) {
        Date now = new Date();
        return Jwts.builder().subject(userId.toString()).claim("role", role)
                .issuedAt(now).expiration(new Date(now.getTime() + exp)).signWith(key).compact();
    }
    public UUID getUserIdFromToken(String token) {
        return UUID.fromString(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject());
    }
    public boolean validateToken(String token) {
        try { Jwts.parser().verifyWith(key).build().parseSignedClaims(token); return true; }
        catch (Exception ex) { log.error("JWT validation failed: {}", ex.getMessage()); return false; }
    }
    public long getAccessTokenExpiration() { return accessTokenExpiration; }
}

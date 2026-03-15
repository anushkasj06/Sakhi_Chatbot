package com.wms.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

@Service
public class JwtTokenService {

    private final JwtProperties jwtProperties;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateAccessToken(Long userId, String email, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
            .issuer(jwtProperties.getIssuer())
            .subject(userId.toString())
            .claim("email", email)
            .claim("roles", roles)
            .claim("type", "access")
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(jwtProperties.getExpirationMinutes(), ChronoUnit.MINUTES)))
            .signWith(getKey())
            .compact();
    }

    public String generateRefreshToken(Long userId, String email, String tokenId) {
        Instant now = Instant.now();
        return Jwts.builder()
            .issuer(jwtProperties.getIssuer())
            .subject(userId.toString())
            .claim("email", email)
            .claim("type", "refresh")
            .id(tokenId == null ? UUID.randomUUID().toString() : tokenId)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(jwtProperties.getRefreshExpirationMinutes(), ChronoUnit.MINUTES)))
            .signWith(getKey())
            .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(getKey())
            .clockSkewSeconds(jwtProperties.getClockSkewSeconds())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public boolean isRefreshToken(Claims claims) {
        return "refresh".equals(claims.get("type", String.class));
    }

    public boolean isAccessToken(Claims claims) {
        return "access".equals(claims.get("type", String.class));
    }

    public long getAccessTokenTtlSeconds() {
        return jwtProperties.getExpirationMinutes() * 60;
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}

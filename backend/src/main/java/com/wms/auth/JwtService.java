package com.wms.auth;

import com.wms.user.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey secretKey;
  private final String issuer;
  private final Duration expiration;
  private final long clockSkewSeconds;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.issuer:wms}") String issuer,
      @Value("${app.jwt.expirationMinutes:60}") long expirationMinutes,
      @Value("${app.jwt.clockSkewSeconds:30}") long clockSkewSeconds
  ) {
    if (secret == null || secret.isBlank()) {
      throw new IllegalArgumentException("JWT secret is required");
    }

    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    if (keyBytes.length < 32) {
      throw new IllegalArgumentException("JWT secret must be at least 32 bytes for HS256");
    }

    this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    this.issuer = issuer;
    this.expiration = Duration.ofMinutes(expirationMinutes);
    this.clockSkewSeconds = clockSkewSeconds;
  }

  public String generateToken(String email, Role role) {
    Instant now = Instant.now();
    return Jwts.builder()
        .issuer(issuer)
        .subject(email)
        .claim("role", role.name())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(expiration)))
        .signWith(secretKey, Jwts.SIG.HS256)
        .compact();
  }

  public String extractSubject(String token) {
    return parseClaims(token).getSubject();
  }

  public Claims parseClaims(String token) {
    return Jwts.parser()
        .clockSkewSeconds(clockSkewSeconds)
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}

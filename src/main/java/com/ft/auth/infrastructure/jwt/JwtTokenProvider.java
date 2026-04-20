package com.ft.auth.infrastructure.jwt;

import com.ft.auth.application.port.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

@Component
public class JwtTokenProvider implements TokenProvider {

    private final SecretKey secretKey;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    @Override
    public String createAccessToken(Long userId) {
        return buildToken(userId, accessExpiration);
    }

    @Override
    public String createRefreshToken(Long userId) {
        return buildToken(userId, refreshExpiration);
    }

    @Override
    public Long getUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    @Override
    public boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public LocalDateTime getRefreshTokenExpiry() {
        return LocalDateTime.now().plusSeconds(refreshExpiration / 1000);
    }

    private String buildToken(Long userId, long expiration) {
        Date now = new Date();
        return Jwts.builder()
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

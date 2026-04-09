package com.agrabandhan.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpiryMs;
    private final long refreshTokenExpiryMs;

    public JwtTokenProvider(
            @Value("${agrabandhan.jwt.secret}") String jwtSecret,
            @Value("${agrabandhan.jwt.access-token-expiry-ms}") long accessTokenExpiryMs,
            @Value("${agrabandhan.jwt.refresh-token-expiry-ms}") long refreshTokenExpiryMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(jwtSecret.getBytes())));
        this.accessTokenExpiryMs = accessTokenExpiryMs;
        this.refreshTokenExpiryMs = refreshTokenExpiryMs;
    }

    public String generateAccessToken(Long userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiryMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiryMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }
}

package com.beta.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.access-token-expiration}") long accessTokenExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public String generateAccessToken(Long userId, String favoriteTeamCode, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);
        
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(ClaimEnum.TYPE.name(), "ACCESS")
                .claim(ClaimEnum.TEAM_CODE.name(), favoriteTeamCode)
                .claim(ClaimEnum.ROLE.name(), role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String getSubject(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Failed to extract subject from token", e);
            return null;
        }
    }

    public <T> T getClaim(String token, String claimName, Class<T> clazz) {
        try {
            Claims claims = getClaims(token);
            return claims.get(claimName, clazz);
        } catch (Exception e) {
            log.error("Failed to extract claim '{}' from token", claimName, e);
            return null;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = getClaims(token);
            return !isTokenExpired(token) && claims.getSubject() != null;
        } catch (Exception e) {
            log.debug("Token validation failed", e);
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            log.debug("Failed to check token expiration", e);
            return true;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public enum ClaimEnum {
        TYPE, TEAM_CODE, ROLE
    }
}

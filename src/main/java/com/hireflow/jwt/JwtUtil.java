package com.hireflow.jwt;

import java.util.Date;
import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(
            String email,
            String role) {

        Map<String, Object> claims =
                new HashMap<>();

        claims.put("role", role);

        return Jwts.builder()

                .claims(claims)

                .subject(email)

                .issuedAt(new Date())

                .expiration(
                    new Date(
                        System.currentTimeMillis()
                        + 1000 * 60 * 60
                    )
                )

                .signWith(getKey())

                .compact();
    }

    public String extractEmail(String token) {

        return Jwts.parser()

                .verifyWith(getKey())

                .build()

                .parseSignedClaims(token)

                .getPayload()

                .getSubject();
    }
    
    public String extractRole(String token) {

        return Jwts.parser()

                .verifyWith(getKey())

                .build()

                .parseSignedClaims(token)

                .getPayload()

                .get("role", String.class);
    }
    
    public String generateRefreshToken(
            String email) {

        return Jwts.builder()

                .subject(email)

                .issuedAt(new Date())

                .expiration(

                    new Date(
                        System.currentTimeMillis()
                        + 1000L * 60 * 60 * 24 * 7
                    )
                )

                .signWith(getKey())

                .compact();
    }
}
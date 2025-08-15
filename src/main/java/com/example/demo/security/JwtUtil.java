package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.Set;

public class JwtUtil {

    private final Key key;
    private final long expirationMs;

    public JwtUtil(String secret, long expirationMs) {
        if (secret != null && secret.length() >= 32) {
            this.key = Keys.hmacShaKeyFor(secret.getBytes());
        } else {
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username, Set<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}


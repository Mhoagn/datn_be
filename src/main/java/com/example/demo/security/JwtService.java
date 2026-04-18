package com.example.demo.security;

import com.example.demo.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final String issuer;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public JwtService(
            @Value("${app.jwt.access-secret}") String accessSecretB64,
            @Value("${app.jwt.refresh-secret}") String refreshSecretB64,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.access-ttl-min}") long accessMin,
            @Value("${app.jwt.refresh-ttl-days}") long refreshDays
    ) {
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecretB64));
        this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecretB64));
        this.issuer = issuer;
        this.accessTtl = Duration.ofMinutes(accessMin);
        this.refreshTtl = Duration.ofDays(refreshDays);
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(user.getId()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTtl)))
                .claim("email", user.getEmail())
                .signWith(accessKey)
                .compact();
    }

    public String createRefreshToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(user.getId()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(refreshTtl)))
                .claim("type", "refresh")
                .signWith(refreshKey)
                .compact();
    }

    public Jws<Claims> parseAccess(String token) {
        return Jwts.parser().verifyWith(accessKey).build().parseSignedClaims(token);
    }

    public Jws<Claims> parseRefresh(String token) {
        return Jwts.parser().verifyWith(refreshKey).build().parseSignedClaims(token);
    }

    public Instant refreshExpiresAt() {
        return Instant.now().plus(refreshTtl);
    }
}

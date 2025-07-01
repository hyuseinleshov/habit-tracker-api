package com.habittracker.api.security.jwt.utils;

import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public final class JwtTestUtils {

    private final SecretKey key;

    public JwtTestUtils(SecretKey key) {
        this.key = key;
    }

    public String generateTestToken(String subject, String issuer, Instant expiration, Instant notBefore) {
        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .subject(subject)
                .issuer(issuer)
                .expiration(Date.from(expiration))
                .notBefore(Date.from(notBefore)).
                signWith(key).compact();
    }

    public String tokenWithInvalidSignature() {
        return Jwts.builder().signWith(Jwts.SIG.HS256.key().build()).compact();
    }

    public String generateTestToken(String subject, String issuer) {
        Instant now = Instant.now();
        return generateTestToken(subject, issuer, now.plus(5, ChronoUnit.MINUTES),
                now.minus(5, ChronoUnit.SECONDS));
    }
}

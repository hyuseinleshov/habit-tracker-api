package com.habittracker.api.security.jwt.service;

import com.habittracker.api.security.jwt.config.JwtProperties;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    private final SecretKey secretKey;
    private final JwtProperties jwtProperties;

    public JwtServiceImpl(SecretKey secretKey, JwtProperties jwtProperties) {
        this.secretKey = secretKey;
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(String email) {
        Instant now = Instant.now();
        String token = Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtProperties.getExpirationDuration())))
                .notBefore(Date.from(now.minus(jwtProperties.getNotBeforeLeewayDuration())))
                .issuer(jwtProperties.getIssuer())
                .signWith(secretKey)
                .compact();
        log.info("Generate token for user with email: {}", email);
        return token;
    }

    @Override
    public boolean isValid(String token) {
        return false;
    }
}

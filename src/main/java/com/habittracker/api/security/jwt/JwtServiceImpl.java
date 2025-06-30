package com.habittracker.api.security.jwt;

import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService{

    private final SecretKey secretKey;
    private final JwtProperties jwtProperties;

    public JwtServiceImpl(SecretKey secretKey, JwtProperties jwtProperties) {
        this.secretKey = secretKey;
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(String email) {
        Instant now = Instant.now();
        return Jwts.builder()
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
    }
}

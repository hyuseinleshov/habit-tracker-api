package com.habittracker.api.security.jwt.service;

import com.habittracker.api.security.jwt.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    private static final Map<Class<? extends JwtException>, String> EXCEPTION_MESSAGES =
            Map.of(
                    MalformedJwtException.class, "Invalid JWT format: the token is malformed and cannot be trusted.",
                    SignatureException.class, "Invalid JWT signature: signature verification failed, token cannot be trusted.",
                    ExpiredJwtException.class, "Expired JWT: the token has expired and is no longer valid.",
                    PrematureJwtException.class, "Premature JWT: the token is not yet valid (not before claim violation).",
                    IncorrectClaimException.class, "JWT claim validation failed: claim (iss) does not match the expected value.",
                    UnsupportedJwtException.class, "Unsupported JWT: the token format or algorithm is not supported.",
                    JwtException.class, "General JWT error: unexpected problem occurred while processing token."
            );

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
        return extractClaims(token).isPresent();
    }

    private Optional<Claims> extractClaims(String token) {
        try {
            return Optional.of((Claims) Jwts.parser()
                    .requireIssuer(jwtProperties.getIssuer())
                    .verifyWith(secretKey)
                    .clockSkewSeconds(jwtProperties.getClockSkewSeconds())
                    .build()
                    .parse(token)
                    .getPayload());
        } catch (JwtException ex) {
            log.warn(EXCEPTION_MESSAGES.get(ex.getClass()));
        } catch (IllegalArgumentException ignored) {
            log.warn("JWT string is null, empty, or only whitespace and cannot be parsed.");
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> extractSubject(String token) {
        return extractClaims(token).map(Claims::getSubject);
    }
}

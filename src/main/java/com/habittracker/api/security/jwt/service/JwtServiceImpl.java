package com.habittracker.api.security.jwt.service;

import com.habittracker.api.security.jwt.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

  @Getter
  private enum JwtValidationError {
    MALFORMED(
        MalformedJwtException.class,
        "Invalid JWT format: the token is malformed and cannot be trusted."),
    SIGNATURE(
        SignatureException.class,
        "Invalid JWT signature: signature verification failed, token cannot be trusted."),
    EXPIRED(
        ExpiredJwtException.class, "Expired JWT: the token has expired and is no longer valid."),
    PREMATURE(
        PrematureJwtException.class,
        "Premature JWT: the token is not yet valid (not before claim violation)."),
    INCORRECT_CLAIMS(
        IncorrectClaimException.class,
        "JWT claim validation failed: claim (iss) does not match the expected value."),
    UNSUPPORTED(
        UnsupportedJwtException.class,
        "Unsupported JWT: the token format or algorithm is not supported."),
    GENERAL(
        JwtException.class,
        "General JWT error: unexpected problem occurred while processing token.");

    private final Class<? extends JwtException> exceptionType;
    private final String message;
    private static final JwtValidationError[] values = JwtValidationError.values();

    JwtValidationError(Class<? extends JwtException> exceptionType, String message) {
      this.exceptionType = exceptionType;
      this.message = message;
    }

    public static String getMessageFor(Class<? extends JwtException> exceptionType) {
      return Arrays.stream(values)
          .filter(e -> e.getExceptionType().equals(exceptionType))
          .map(JwtValidationError::getMessage)
          .findFirst()
          .orElse("Unknow JWT error");
    }
  }

  private final SecretKey secretKey;
  private final JwtProperties jwtProperties;

  public JwtServiceImpl(SecretKey secretKey, JwtProperties jwtProperties) {
    this.secretKey = secretKey;
    this.jwtProperties = jwtProperties;
  }

  public String generateToken(String email) {
    Instant now = Instant.now();
    String token =
        Jwts.builder()
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
      return Optional.of(
          (Claims)
              Jwts.parser()
                  .requireIssuer(jwtProperties.getIssuer())
                  .verifyWith(secretKey)
                  .clockSkewSeconds(jwtProperties.getClockSkewSeconds())
                  .build()
                  .parse(token)
                  .getPayload());
    } catch (JwtException ex) {
      log.warn(JwtValidationError.getMessageFor(ex.getClass()));
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

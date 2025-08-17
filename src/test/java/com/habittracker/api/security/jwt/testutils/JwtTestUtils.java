package com.habittracker.api.security.jwt.testutils;

import static com.habittracker.api.config.constants.JwtTestConstant.*;

import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.stream.Stream;
import javax.crypto.SecretKey;

public final class JwtTestUtils {

  private JwtTestUtils() {}

  private static String generateTestToken(
      String subject, String issuer, Instant expiration, Instant notBefore, SecretKey key) {
    return Jwts.builder()
        .header()
        .type("JWT")
        .and()
        .subject(subject)
        .issuer(issuer)
        .expiration(Date.from(expiration))
        .notBefore(Date.from(notBefore))
        .signWith(key)
        .compact();
  }

  public static Stream<String> getInvalidTokens() {
    final String DUMMY_ISSUER = "dummy issuer";
    Instant now = Instant.now();
    return Stream.of(
        "mailFormedJwt",
        tokenWithInvalidSignature(),
        generateTestToken(
            TEST_SUBJECT, DUMMY_ISSUER, now.minus(2, ChronoUnit.MINUTES), now, TEST_SECRET_KEY),
        generateTestToken(
            TEST_SUBJECT,
            DUMMY_ISSUER,
            now.plus(20, ChronoUnit.MINUTES),
            now.plus(5, ChronoUnit.MINUTES),
            TEST_SECRET_KEY),
        generateTestToken(
            TEST_SUBJECT,
            DUMMY_ISSUER,
            now.plus(20, ChronoUnit.MINUTES),
            now.minus(5, ChronoUnit.SECONDS),
            TEST_SECRET_KEY),
        "",
        null);
  }

  private static String tokenWithInvalidSignature() {
    return Jwts.builder().signWith(Jwts.SIG.HS256.key().build()).compact();
  }

  public static String generateValidToken(String subject, String issuer, SecretKey key) {
    Instant now = Instant.now();
    return generateTestToken(
        subject, issuer, now.plus(5, ChronoUnit.MINUTES), now.minus(5, ChronoUnit.SECONDS), key);
  }
}

package com.habittracker.api.security.jwt.utils;

import static com.habittracker.api.security.jwt.utils.JwtTestConstant.*;

import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.stream.Stream;

public final class JwtTestUtils {

  private JwtTestUtils() {}

  private static String generateTestToken(String issuer, Instant expiration, Instant notBefore) {
    return Jwts.builder()
        .header()
        .type("JWT")
        .and()
        .subject(TEST_SUBJECT)
        .issuer(issuer)
        .expiration(Date.from(expiration))
        .notBefore(Date.from(notBefore))
        .signWith(TEST_SECRET_KEY)
        .compact();
  }

  public static Stream<String> getInvalidTokens() {
    final String DUMMY_ISSUER = "dummy issuer";
    Instant now = Instant.now();
    return Stream.of(
        "mailFormedJwt",
        tokenWithInvalidSignature(),
        generateTestToken(DUMMY_ISSUER, now.minus(2, ChronoUnit.MINUTES), now),
        generateTestToken(
            DUMMY_ISSUER, now.plus(20, ChronoUnit.MINUTES), now.plus(5, ChronoUnit.MINUTES)),
        generateTestToken(
            DUMMY_ISSUER, now.plus(20, ChronoUnit.MINUTES), now.minus(5, ChronoUnit.SECONDS)),
        "",
        null);
  }

  private static String tokenWithInvalidSignature() {
    return Jwts.builder().signWith(Jwts.SIG.HS256.key().build()).compact();
  }

  public static String generateValidToken() {
    Instant now = Instant.now();
    return generateTestToken(
        TEST_ISSUER, now.plus(5, ChronoUnit.MINUTES), now.minus(5, ChronoUnit.SECONDS));
  }
}

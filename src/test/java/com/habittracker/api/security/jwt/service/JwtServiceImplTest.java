package com.habittracker.api.security.jwt.service;

import static com.habittracker.api.security.jwt.utils.JwtTestConstant.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.habittracker.api.security.jwt.config.JwtProperties;
import com.habittracker.api.security.jwt.utils.JwtTestUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JwtServiceImplTest {

  private static final Duration EXPIRATION_DURATION = Duration.of(15, ChronoUnit.HOURS);
  private static final Duration NOT_BEFORE_LEEWAY_DURATION = Duration.of(7, ChronoUnit.MINUTES);
  private static final long TOLERANCE_SECONDS = 1;

  @Mock private JwtProperties jwtProperties;

  private JwtServiceImpl totest;

  @BeforeEach
  void setUp() {
    totest = new JwtServiceImpl(TEST_SECRET_KEY, jwtProperties);
  }

  private void setUpJwtProperties() {
    when(jwtProperties.getIssuer()).thenReturn(TEST_ISSUER);
    when(jwtProperties.getExpirationDuration()).thenReturn(EXPIRATION_DURATION);
    when(jwtProperties.getNotBeforeLeewayDuration()).thenReturn(NOT_BEFORE_LEEWAY_DURATION);
  }

  @Test
  public void generateToken_shouldBuildCorrectTokenWithTimestampsAndClaims() {
    setUpJwtProperties();
    String token = totest.generateToken(TEST_SUBJECT);
    Instant now = Instant.now();
    Claims claims =
        (Claims) Jwts.parser().verifyWith(TEST_SECRET_KEY).build().parse(token).getPayload();
    assertEquals(TEST_SUBJECT, claims.getSubject());
    assertEquals(TEST_ISSUER, claims.getIssuer());
    assertTrue(secondsBetween(now, claims.getIssuedAt().toInstant()) <= TOLERANCE_SECONDS);
    assertTrue(
        secondsBetween(now, claims.getExpiration().toInstant().minus(EXPIRATION_DURATION))
            <= TOLERANCE_SECONDS);
    assertTrue(
        secondsBetween(now, claims.getNotBefore().toInstant().plus(NOT_BEFORE_LEEWAY_DURATION))
            > TOLERANCE_SECONDS);
  }

  private long secondsBetween(Instant start, Instant end) {
      return Duration.between(start, end).abs().toSeconds();
  }

  @ParameterizedTest
  @MethodSource("com.habittracker.api.security.jwt.utils.JwtTestUtils#getInvalidTokens")
  public void isValid_shouldReturnExpectedResult_forGivenTokens(String token) {
    when(jwtProperties.getIssuer()).thenReturn(TEST_ISSUER);
    when(jwtProperties.getClockSkewSeconds()).thenReturn(20);
    assertFalse(totest.isValid(token));
  }

  @Test
  public void isValid_shouldReturnTrue_whenTokenIsValid() {
    when(jwtProperties.getIssuer()).thenReturn(TEST_ISSUER);
    when(jwtProperties.getClockSkewSeconds()).thenReturn(20);
    String validToken = JwtTestUtils.generateValidToken();
    assertTrue(totest.isValid(validToken));
  }

  @ParameterizedTest
  @MethodSource("com.habittracker.api.security.jwt.utils.JwtTestUtils#getInvalidTokens")
  public void extractSubject_shouldReturnEmpty_forGivenTokens(String token) {
    when(jwtProperties.getIssuer()).thenReturn(TEST_ISSUER);
    when(jwtProperties.getClockSkewSeconds()).thenReturn(20);
    assertTrue(totest.extractSubject(token).isEmpty());
  }

  @Test
  public void extractSubject_shouldReturnSubject_whenTokenIsValid() {
    when(jwtProperties.getIssuer()).thenReturn(TEST_ISSUER);
    when(jwtProperties.getClockSkewSeconds()).thenReturn(20);
    String token = JwtTestUtils.generateValidToken();
    Optional<String> subjectOptional = totest.extractSubject(token);
    assertTrue(subjectOptional.isPresent());
    String subject = subjectOptional.get();
    assertEquals(TEST_SUBJECT, subject);
  }
}
